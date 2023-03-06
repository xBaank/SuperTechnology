package resa.rodriguez.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toSet
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import resa.rodriguez.config.APIConfig
import resa.rodriguez.config.security.jwt.JwtTokensUtils
import resa.rodriguez.dto.*
import resa.rodriguez.exceptions.UserExceptionBadRequest
import resa.rodriguez.mappers.toDTO
import resa.rodriguez.mappers.toDTOlist
import resa.rodriguez.models.Address
import resa.rodriguez.models.User
import resa.rodriguez.repositories.address.AddressRepositoryCached
import resa.rodriguez.services.*
import resa.rodriguez.services.storage.StorageService
import resa.rodriguez.validators.validate
import java.util.*

private val log = KotlinLogging.logger {}

/**
 * Controlador para el manejo de distintos servicios y utiles relacionados con los usuarios
 *
 * @property service
 * @property AddressRepositoryCached
 * @property authenticationManager
 * @property jwtTokenUtils
 * @property StorageService
 */
@RestController
@RequestMapping(APIConfig.API_PATH)
class UserController
@Autowired constructor(
    private val service: UserService,
    private val aRepo: AddressRepositoryCached,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenUtils: JwtTokensUtils,
    private val storageService: StorageService
) {

    // -- GET DEFAULT --
    
    @Operation(summary = "Bienvenida 1", description = "Metodo que devuelve un mensaje de bienvenida.", tags = ["USER"])
    @ApiResponse(responseCode = "200", description = "Mensaje de bienvenida.")
    @GetMapping("")
    fun bienvenida() = ResponseEntity(
        "Micro servicio de gestión de usuarios de una tienda de tecnología para las asignaturas de Acceso a Datos y " +
                "Programación de Procesos y Servicios del IES Luis Vives (Leganés) curso 22/23.",
        HttpStatus.OK
    )

    @Operation(summary = "Bienvenida 2", description = "Metodo que devuelve un mensaje de bienvenida.", tags = ["USER"])
    @ApiResponse(responseCode = "200", description = "Mensaje de bienvenida.")
    @GetMapping("/")
    fun bienvenida2() = ResponseEntity(
        "Micro servicio de gestión de usuarios de una tienda de tecnología para las asignaturas de Acceso a Datos y " +
                "Programación de Procesos y Servicios del IES Luis Vives (Leganés) curso 22/23.",
        HttpStatus.OK
    )

    // -- USERS --

    // Register, Create & Login Methods
    
    @Operation(summary = "Register", description = "Metodo para registrarse.", tags = ["USER"])
    @Parameter(name = "userDTO", description = "DTO de registro valido.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el DTO de visualizacion del usuario registrado y su token JWT.")
    @ApiResponse(responseCode = "400", description = "Cuando no puede llevar a cabo el registro.")
    @PostMapping("/register")
    suspend fun register(@Valid @RequestBody userDto: UserDTOregister): ResponseEntity<UserDTOwithToken> =
        withContext(Dispatchers.IO) {
            log.info { "Registro de usuario: ${userDto.username}" }

            userDto.validate()
            try {
                val userSaved = service.register(userDto)
                val addresses = userSaved.id?.let { service.findAllFromUserId(it).toSet() } ?: setOf()

                ResponseEntity.ok(UserDTOwithToken(userSaved.toDTO(addresses), jwtTokenUtils.create(userSaved)))
            } catch (e: Exception) {
                throw UserExceptionBadRequest(e.message)
            }
        }

    @Operation(summary = "Create", description = "Metodo para que los administradores puedan crear usuarios.", tags = ["USER"])
    @Parameter(name = "userDTOcreate", description = "DTO de creacion valido.", required = true)
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el DTO de visualizacion del usuario creado y su token JWT.")
    @ApiResponse(responseCode = "400", description = "Cuando no puede llevar a cabo la creacion debido a que el DTO es invalido.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/create")
    suspend fun create(
        @Valid @RequestBody userDTOcreate: UserDTOcreate,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<UserDTOwithToken> =
        withContext(Dispatchers.IO) {
            log.info { "Creando usuario por parte de un administrador" }

            userDTOcreate.validate()
            val userSaved = service.create(userDTOcreate)
            val addresses = userSaved.id?.let { service.findAllFromUserId(it).toSet() } ?: setOf()

            ResponseEntity(
                UserDTOwithToken(userSaved.toDTO(addresses), jwtTokenUtils.create(userSaved)),
                HttpStatus.CREATED
            )
        }

    // El createByAdmin que se usara por parte del cliente es el superior, este simplemente es para la carga de datos inicial
    suspend fun createByAdminInitializer(userDTOcreate: UserDTOcreate): ResponseEntity<UserDTOwithToken> =
        withContext(Dispatchers.IO) {
            log.info { "Creando usuario por parte de un administrador || Carga de datos inicial" }

            userDTOcreate.validate()
            val userSaved = service.create(userDTOcreate)
            val addresses = userSaved.id?.let { service.findAllFromUserId(it).toSet() } ?: setOf()

            ResponseEntity(UserDTOwithToken(userSaved.toDTO(addresses), jwtTokenUtils.create(userSaved)), HttpStatus.CREATED)
        }

    @Operation(summary = "Login", description = "Metodo para el logado.", tags = ["USER"])
    @Parameter(name = "userDTOlogin", description = "DTO de logado valido.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el DTO de visualizacion del usuario y su token JWT.")
    @ApiResponse(responseCode = "400", description = "Cuando no puede llevar a cabo el logado debido a que el DTO es invalido.")
    @GetMapping("/login")
    suspend fun login(@Valid @RequestBody userDto: UserDTOlogin): ResponseEntity<UserDTOwithToken> {
        log.info { "Login de usuario: ${userDto.username}" }

        userDto.validate()
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                userDto.username,
                userDto.password
            )
        )
        // Autenticamos al usuario, si lo es nos lo devuelve
        SecurityContextHolder.getContext().authentication = authentication

        // Devolvemos al usuario autenticado
        val user = authentication.principal as User
        val addresses = user.id?.let { service.findAllFromUserId(it).toSet() } ?: setOf()

        return ResponseEntity.ok(UserDTOwithToken(user.toDTO(addresses), jwtTokenUtils.create(user)))
    }

    // "Find All" Methods
    
    @Operation(summary = "List users", description = "Metodo para encontrar a todos los usuarios.", tags = ["USER"])
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con una lista de DTO de visualizacion de todos los usuarios.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list")
    suspend fun listUsers(@AuthenticationPrincipal user: User): ResponseEntity<List<UserDTOresponse>> {
        log.info { "Obteniendo listado de usuarios" }

        val res = service.listUsers()
        return ResponseEntity.ok(res.toDTOlist(aRepo))
    }

    @Operation(summary = "Find All Paging", description = "Metodo para encontrar a todos los usuarios de forma paginada.", tags = ["USER"])
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @Parameter(name = "page", description = "Numero de pagina.", required = false)
    @Parameter(name = "size", description = "Tamaño de pagina.", required = false)
    @Parameter(name = "sortBy", description = "Tipo de ordenacion.", required = false)
    @ApiResponse(responseCode = "200", description = "Response Entity con una pagina de DTO de visualizacion de usuarios.")
    @ApiResponse(responseCode = "404", description = "Cuando no existe esa pagina.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list/paging")
    suspend fun getAllPaging(
        @AuthenticationPrincipal user: User,
        @RequestParam(defaultValue = APIConfig.PAGINATION_INIT) page: Int = 0,
        @RequestParam(defaultValue = APIConfig.PAGINATION_SIZE) size: Int = 10,
        @RequestParam(defaultValue = APIConfig.PAGINATION_SORT) sortBy: String = "created_at",
    ): ResponseEntity<Page<UserDTOresponse>> {
        log.info { "Buscando usuarios paginados || Pagina: $page" }

        val pageResponse = service.findAllPaging(page, size, sortBy)

        return ResponseEntity.ok(pageResponse)
    }

    @Operation(summary = "List Users Active", description = "Metodo para encontrar a todos los usuarios filtrados por su actividad.", tags = ["USER"])
    @Parameter(name = "active", description = "Tipo de actividad.", required = true)
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con una lista de DTO de visualizacion de usuarios con el tipo de actividad pasado por parametro.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list/{active}")
    suspend fun listUsersActive(
        @PathVariable active: Boolean,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<List<UserDTOresponse>> {
        log.info { "Obteniendo listado de usuarios activados" }

        val res = service.findAllByActive(active)

        return ResponseEntity.ok(res.toDTOlist(aRepo))
    }

    // "Find One" Methods
    
    @Operation(summary = "Find By Username", description = "Metodo para encontrar a un usuario por su username.", tags = ["USER"])
    @Parameter(name = "u", description = "Token de autenticacion.", required = true)
    @Parameter(name = "username", description = "Nombre de usuario a buscar.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el DTO de visualizacion del usuario.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra al usuario.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/username/{username}")
    suspend fun findByUsername(
        @AuthenticationPrincipal u: User,
        @PathVariable username: String,
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con username: $username" }

            val user = service.findByUsername(username)
            val addresses = service.findAllFromUserId(user.id!!).toSet()

            ResponseEntity.ok(user.toDTO(addresses))
        }

    @Operation(summary = "Find By Id", description = "Metodo para encontrar a un usuario por su id.", tags = ["USER"])
    @Parameter(name = "u", description = "Token de autenticacion.", required = true)
    @Parameter(name = "userId", description = "UUID a buscar.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el DTO de visualizacion del usuario.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra al usuario.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/id/{userId}")
    suspend fun findByUserId(
        @AuthenticationPrincipal u: User,
        @PathVariable userId: UUID,
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con id: $userId" }

            val user = service.findById(userId)
            val addresses = service.findAllFromUserId(user.id!!).toSet()

            ResponseEntity.ok(user.toDTO(addresses))
        }

    @Operation(summary = "Find By Email", description = "Metodo para encontrar a un usuario por su email.", tags = ["USER"])
    @Parameter(name = "u", description = "Token de autenticacion.", required = true)
    @Parameter(name = "userEmail", description = "Email a buscar.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el DTO de visualizacion del usuario.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra al usuario.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/email/{userEmail}")
    suspend fun findByUserEmail(
        @AuthenticationPrincipal u: User,
        @PathVariable userEmail: String
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con email: $userEmail" }

            val user = service.findByEmail(userEmail)
            val addresses = service.findAllFromUserId(user.id!!).toSet()

            ResponseEntity.ok(user.toDTO(addresses))
        }

    @Operation(summary = "Find By Phone", description = "Metodo para encontrar a un usuario por su telefono.", tags = ["USER"])
    @Parameter(name = "u", description = "Token de autenticacion.", required = true)
    @Parameter(name = "userPhone", description = "Telefono a buscar.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el DTO de visualizacion del usuario.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra al usuario.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/phone/{userPhone}")
    suspend fun findByUserPhone(
        @AuthenticationPrincipal u: User,
        @PathVariable userPhone: String
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con telefono: $userPhone" }

            val user = service.findByUserPhone(userPhone)
            val addresses = service.findAllFromUserId(user.id!!).toSet()

            ResponseEntity.ok(user.toDTO(addresses))
        }

    // "Update" Methods

    @Operation(summary = "Update Myself", description = "Metodo para cambiar la contraseña o las direcciones de tu propio usuario.", tags = ["USER"])
    @Parameter(name = "u", description = "Token de autenticacion.", required = true)
    @Parameter(name = "userDTOUpdated", description = "DTO valido con los datos a cambiar.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el DTO de visualizacion de tu usuario.")
    @ApiResponse(responseCode = "400", description = "Cuando el DTO no es valido.")
    @PutMapping("/me")
    suspend fun updateMySelf(
        @AuthenticationPrincipal user: User,
        @Valid @RequestBody userDTOUpdated: UserDTOUpdated
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        log.info { "Actualizando usuario" }

        userDTOUpdated.validate()
        val userSaved = service.updateMySelf(user, userDTOUpdated)
        val addresses = userSaved.id?.let { service.findAllFromUserId(it).toSet() } ?: setOf()

        ResponseEntity.ok(userSaved.toDTO(addresses))
    }

    @Operation(summary = "Update Avatar", description = "Metodo para cambiar el avatar de tu propio usuario.", tags = ["USER"])
    @Parameter(name = "u", description = "Token de autenticacion.", required = true)
    @Parameter(name = "file", description = "Avatar nuevo.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el DTO de visualizacion de tu usuario.")
    @ApiResponse(responseCode = "400", description = "Cuando no puede guardar el archivo o no puede resolver el tipo de archivo que es.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra el archivo o no lo puede leer.")
    @PutMapping("/me/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun updateAvatar(
        @AuthenticationPrincipal user: User,
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<UserDTOresponse> = runBlocking {
        log.info { "Actualizando avatar usuario" }

        val myScope = CoroutineScope(Dispatchers.IO)
        val fileStored = myScope.async { storageService.storeFileFromUser(file, user.username) }.await()

        val uriImage = storageService.getUrl(fileStored)

        val newUser = user.copy(
            avatar = uriImage
        )

        val userUpdated = service.updateAvatar(newUser)
        val addresses = userUpdated.id?.let { service.findAllFromUserId(it).toSet() } ?: setOf()

        return@runBlocking ResponseEntity.ok(userUpdated.toDTO(addresses))
    }

    @Operation(summary = "Switch Activity By Email", description = "Metodo para cambiar el tipo de actividad de un usuario buscado por su email.", tags = ["USER"])
    @Parameter(name = "email", description = "Email a buscar.", required = true)
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el DTO de visualizacion del usuario.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra el usuario.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/activity/{email}")
    suspend fun switchActivityByEmail(
        @PathVariable email: String,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Cambio de actividad por email" }

            val userSaved = service.switchActivity(email)
            val addresses = userSaved.id?.let { service.findAllFromUserId(it).toSet() } ?: setOf()

            ResponseEntity.ok(userSaved.toDTO(addresses))
        }

    @Operation(summary = "Update Role By Email", description = "Metodo para cambiar el rol de un usuario buscado por su email.", tags = ["USER"])
    @Parameter(name = "userDTORoleUpdated", description = "DTO valido con el nuevo rol y el email del usuario.", required = true)
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el DTO de visualizacion del usuario.")
    @ApiResponse(responseCode = "400", description = "Cuando el DTO es invalido.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra el usuario.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/role")
    suspend fun updateRoleByEmail(
        @Valid @RequestBody userDTORoleUpdated: UserDTORoleUpdated,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        log.info { "Actualizando rol de usuario con email: ${userDTORoleUpdated.email}" }

        userDTORoleUpdated.validate()
        val userSaved = service.updateRoleByEmail(userDTORoleUpdated)
        val addresses = userSaved.id?.let { service.findAllFromUserId(it).toSet() } ?: setOf()

        ResponseEntity.ok(userSaved.toDTO(addresses))
    }

    // "Delete" Methods
    
    @Operation(summary = "Delete User", description = "Metodo para borrar un usuario buscado por su email.", tags = ["USER"])
    @Parameter(name = "email", description = "Email del usuario a borrar.", required = true)
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el DTO de visualizacion del usuario.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra el usuario.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{email}")
    suspend fun deleteUser(
        @PathVariable email: String,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        log.info { "Eliminando al usuario de forma definitiva junto a sus direcciones asociadas" }

        val deleted = service.delete(email)
        val addresses = deleted.id?.let { service.findAllFromUserId(it).toSet() } ?: setOf()

        ResponseEntity.ok(deleted.toDTO(addresses))
    }

    // "Me" Method
    
    @Operation(summary = "Find Myself", description = "Metodo para buscarse a uno mismo.", tags = ["USER"])
    @Parameter(name = "userDTORoleUpdated", description = "DTO valido con el nuevo rol y el email del usuario.", required = true)
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el DTO de visualizacion del usuario.")
    @GetMapping("/me")
    suspend fun findMySelf(@AuthenticationPrincipal user: User): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo datos del usuario." }
            val addresses = user.id?.let { service.findAllFromUserId(it).toSet() } ?: setOf()

            ResponseEntity.ok(user.toDTO(addresses))
        }

    // -- ADDRESSES --

    // "Find All" Methods
    
    @Operation(summary = "List Addresses", description = "Metodo para encontrar todas las direcciones registradas.", tags = ["ADDRESS"])
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con una lista de todas las direcciones.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list/address")
    suspend fun listAddresses(@AuthenticationPrincipal user: User): ResponseEntity<List<Address>> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo listado de direcciones" }

            ResponseEntity.ok(service.findAllAddresses())
        }

    @Operation(summary = "List Addresses Paginadas", description = "Metodo para encontrar todas las direcciones registradas de forma paginada.", tags = ["ADDRESS"])
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @Parameter(name = "page", description = "Numero de la pagina.", required = false)
    @Parameter(name = "size", description = "Tamaño de la pagina.", required = false)
    @Parameter(name = "sortBy", description = "Forma de ordenacion.", required = false)
    @ApiResponse(responseCode = "200", description = "Response Entity con una pagina de direcciones.")
    @ApiResponse(responseCode = "404", description = "Cuando la pagina no existe.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list/address/paging")
    suspend fun getAllPagingAddresses(
        @AuthenticationPrincipal user: User,
        @RequestParam(defaultValue = APIConfig.PAGINATION_INIT) page: Int = 0,
        @RequestParam(defaultValue = APIConfig.PAGINATION_SIZE) size: Int = 10,
        @RequestParam(defaultValue = APIConfig.PAGINATION_SORT) sortBy: String = "created_at",
    ): ResponseEntity<Page<Address>> {
        log.info { "Buscando direcciones paginadas || Pagina: $page" }

        val pageResponse = service.findAllPagingAddresses(page, size, sortBy)

        return ResponseEntity.ok(pageResponse)
    }

    @Operation(summary = "List Addresses By User ID", description = "Metodo para encontrar todas las direcciones de un usuario concreto.", tags = ["ADDRESS"])
    @Parameter(name = "userId", description = "UUID del usuario cuyas direcciones queremos encontrar.", required = true)
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con una cadena de texto que contiene todas las direcciones de ese usuario separadas por comas.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra ninguna direccion.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list/address/{userId}")
    suspend fun listAddressesByUserId(
        @PathVariable userId: UUID,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo direcciones de usuario con id: $userId" }

        ResponseEntity.ok(service.listAddressesByUserId(userId))
    }

    // "Find One" Methods
    
    @Operation(summary = "Find By ID", description = "Metodo para encontrar una direccion por su UUID.", tags = ["ADDRESS"])
    @Parameter(name = "id", description = "UUID de la direccion.", required = true)
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el nombre de la direccion.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra ninguna direccion con ese UUID.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/address/{id}")
    suspend fun findById(@PathVariable id: UUID, @AuthenticationPrincipal user: User): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo direccion con id: $id" }

            ResponseEntity.ok(service.findAddressById(id))
        }

    @Operation(summary = "Find By Name", description = "Metodo para encontrar una direccion por su nombre.", tags = ["ADDRESS"])
    @Parameter(name = "name", description = "Nombre de la direccion.", required = false)
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el nombre de la direccion.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra ninguna direccion con ese nombre.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/address")
    suspend fun findByName(
        @RequestParam(defaultValue = "") name: String = "",
        @AuthenticationPrincipal user: User
    ): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Buscando direccion con nombre: $name" }

            ResponseEntity.ok(service.findAddressByName(name))
        }

    // "Delete" Methods
    
    @Operation(summary = "Delete My Address", description = "Metodo para borrar una direccion de tu usuario por su nombre.", tags = ["ADDRESS"])
    @Parameter(name = "name", description = "Nombre de la direccion.", required = false)
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el nombre de la direccion.")
    @ApiResponse(responseCode = "400", description = "Cuando no consigue borrar la direccion.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra ninguna direccion con ese nombre o ningun usuario con ese email.")
    @DeleteMapping("/me/address")
    suspend fun deleteMyAddress(
        @RequestParam(defaultValue = "") name: String = "",
        @AuthenticationPrincipal user: User
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Eliminando direccion: $name" }

        ResponseEntity.ok(service.deleteAddress(name, user.email))
    }

    @Operation(summary = "Delete Address", description = "Metodo para borrar una direccion por su nombre.", tags = ["ADDRESS"])
    @Parameter(name = "name", description = "Nombre de la direccion.", required = false)
    @Parameter(name = "email", description = "Email del usuario.", required = false)
    @Parameter(name = "user", description = "Token de autenticacion.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el nombre de la direccion.")
    @ApiResponse(responseCode = "400", description = "Cuando no consigue borrar la direccion.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra ninguna direccion con ese nombre o ningun usuario con ese email.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/address")
    suspend fun deleteAddress(
        @RequestParam(defaultValue = "") name: String = "",
        @RequestParam(defaultValue = "") email: String = "",
        @AuthenticationPrincipal user: User
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Eliminando direccion: $name" }

        ResponseEntity.ok(service.deleteAddress(name, email))
    }
}