package resa.rodriguez.controllers

import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.withContext
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
import resa.rodriguez.exceptions.UserExceptionNotFound
import resa.rodriguez.mappers.toDTO
import resa.rodriguez.mappers.toDTOlist
import resa.rodriguez.models.Address
import resa.rodriguez.models.User
import resa.rodriguez.repositories.address.AddressRepositoryCached
import resa.rodriguez.services.*
import resa.rodriguez.validators.validate
import java.util.*

private val log = KotlinLogging.logger {}

/**
 * Controlador para el manejo de distintos repositorios
 *
 * @property service
 * @property AddressRepositoryCached
 * @property authenticationManager
 * @property jwtTokenUtils
 */
@RestController
@RequestMapping(APIConfig.API_PATH)
class UserController
@Autowired constructor(
    private val service: UserService,
    private val aRepo: AddressRepositoryCached,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenUtils: JwtTokensUtils,
) {

    // -- GET DEFAULT --
    @GetMapping("")
    fun bienvenida() = ResponseEntity(
        "Microservicio de gestión de usuarios de una tienda de tecnología para las asignaturas de Acceso a Datos y " +
                "Programación de Procesos y Servicios del IES Luis Vives (Leganés) curso 22/23.",
        HttpStatus.OK
    )

    @GetMapping("/")
    fun bienvenida2() = ResponseEntity(
        "Microservicio de gestión de usuarios de una tienda de tecnología para las asignaturas de Acceso a Datos y " +
                "Programación de Procesos y Servicios del IES Luis Vives (Leganés) curso 22/23.",
        HttpStatus.OK
    )

    // -- USERS --

    // Register, Create & Login Methods
    @PostMapping("/register")
    suspend fun register(@Valid @RequestBody userDto: UserDTOregister): ResponseEntity<UserDTOwithToken> =
        withContext(Dispatchers.IO) {
            log.info { "Registro de usuario: ${userDto.username}" }

            userDto.validate()
            try {
                val userSaved = service.register(userDto)
                val addresses = userSaved.id?.let { aRepo.findAllFromUserId(it).toSet() } ?: setOf()

                ResponseEntity.ok(UserDTOwithToken(userSaved.toDTO(addresses), jwtTokenUtils.create(userSaved)))
            } catch (e: Exception) {
                throw UserExceptionBadRequest(e.message)
            }
        }

    @PostMapping("/create")
    suspend fun create(
        @Valid @RequestBody userDTOcreate: UserDTOcreate,
    ): ResponseEntity<UserDTOwithToken> =
        withContext(Dispatchers.IO) {
            log.info { "Creando usuario por parte de un administrador" }

            userDTOcreate.validate()
            val userSaved = service.create(userDTOcreate)
            val addresses = userSaved.id?.let { aRepo.findAllFromUserId(it).toSet() } ?: setOf()

            ResponseEntity(UserDTOwithToken(userSaved.toDTO(addresses), jwtTokenUtils.create(userSaved)), HttpStatus.CREATED)
        }

    // El createByAdmin que se usara por parte del cliente es el superior, este simplemente es para la carga de datos inicial
    suspend fun createByAdminInitializer(userDTOcreate: UserDTOcreate): ResponseEntity<UserDTOwithToken> =
        withContext(Dispatchers.IO) {
            log.info { "Creando usuario por parte de un administrador || Carga de datos inicial" }

            userDTOcreate.validate()
            val userSaved = service.create(userDTOcreate)
            val addresses = userSaved.id?.let { aRepo.findAllFromUserId(it).toSet() } ?: setOf()

            ResponseEntity.ok(UserDTOwithToken(userSaved.toDTO(addresses), jwtTokenUtils.create(userSaved)))
        }

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
        val addresses = user.id?.let { aRepo.findAllFromUserId(it).toSet() } ?: setOf()

        return ResponseEntity.ok(UserDTOwithToken(user.toDTO(addresses), jwtTokenUtils.create(user)))
    }

    // "Find All" Methods
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list")
    suspend fun listUsers(@AuthenticationPrincipal user: User): ResponseEntity<List<UserDTOresponse>> {
        log.info { "Obteniendo listado de usuarios" }

        val res = service.listUsers()
        return ResponseEntity.ok(res.toDTOlist(aRepo))
    }

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

        return if (pageResponse != null) {
            ResponseEntity.ok(pageResponse)
        } else throw UserExceptionNotFound("Page not found.")
    }

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

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/email/{userEmail}")
    suspend fun findByUserEmail(
        @AuthenticationPrincipal u: User,
        @PathVariable userEmail: String,
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con email: $userEmail" }

            val user = service.findByEmail(userEmail)
            val addresses = service.findAllFromUserId(user.id!!).toSet()

            ResponseEntity.ok(user.toDTO(addresses))
        }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/phone/{userPhone}")
    suspend fun findByUserPhone(
        @PathVariable userPhone: String,
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con telefono: $userPhone" }

            val user = service.findByUserPhone(userPhone)
            val addresses = service.findAllFromUserId(user.id!!).toSet()

            ResponseEntity.ok(user.toDTO(addresses))
        }

    // "Update" Methods

    @PutMapping("/me")
    suspend fun updateMySelf(
        @AuthenticationPrincipal user: User,
        @Valid @RequestBody userDTOUpdated: UserDTOUpdated
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        log.info { "Actualizando usuario" }

        userDTOUpdated.validate()
        val userSaved = service.updateMySelf(user, userDTOUpdated)
        val addresses = userSaved.id?.let { aRepo.findAllFromUserId(it).toSet() } ?: setOf()

        ResponseEntity.ok(userSaved.toDTO(addresses))
    }

    @PutMapping("/me/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun updateAvatar(
        @AuthenticationPrincipal user: User,
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        val userSaved = service.updateAvatar(user, file)
        val addresses = userSaved.id?.let { aRepo.findAllFromUserId(it).toSet() } ?: setOf()

        ResponseEntity.ok(userSaved.toDTO(addresses))
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/activity/{email}")
    suspend fun switchActivityByEmail(
        @PathVariable email: String,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Cambio de actividad por email" }

            val userSaved = service.switchActivity(email)
            val addresses = userSaved.id?.let { aRepo.findAllFromUserId(it).toSet() } ?: setOf()

            ResponseEntity.ok(userSaved.toDTO(addresses))
        }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/role")
    suspend fun updateRoleByEmail(
        @Valid @RequestBody userDTORoleUpdated: UserDTORoleUpdated,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        log.info { "Actualizando rol de usuario con email: ${userDTORoleUpdated.email}" }

        userDTORoleUpdated.validate()
        val userSaved = service.updateRoleByEmail(userDTORoleUpdated)
        val addresses = userSaved.id?.let { aRepo.findAllFromUserId(it).toSet() } ?: setOf()

        ResponseEntity.ok(userSaved.toDTO(addresses))
    }

    // "Delete" Methods
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{email}")
    suspend fun deleteUser(
        @PathVariable email: String,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        log.info { "Eliminando al usuario de forma definitiva junto a sus direcciones asociadas" }

        val deleted = service.delete(email)
        val addresses = deleted.id?.let { aRepo.findAllFromUserId(it).toSet() } ?: setOf()

        ResponseEntity.ok(deleted.toDTO(addresses))
    }

    // "Me" Method
    @GetMapping("/me")
    suspend fun findMySelf(@AuthenticationPrincipal user: User): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo datos del usuario." }
            val addresses = user.id?.let { aRepo.findAllFromUserId(it).toSet() } ?: setOf()

            ResponseEntity.ok(user.toDTO(addresses))
        }

    // -- ADDRESSES --

    // "Find All" Methods
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list/address")
    suspend fun listAddresses(@AuthenticationPrincipal user: User): ResponseEntity<List<Address>> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo listado de direcciones" }

            ResponseEntity.ok(service.findAllAddresses())
        }

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
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/address/{id}")
    suspend fun findById(@PathVariable id: UUID, @AuthenticationPrincipal user: User): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo direccion con id: $id" }

            ResponseEntity.ok(service.findAddressById(id))
        }

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

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/address")
    suspend fun deleteAddress(
        @RequestParam(defaultValue = "") name: String = "",
        @AuthenticationPrincipal user: User
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Eliminando direccion: $name" }

        ResponseEntity.ok(service.deleteAddress(name, user))
    }
}