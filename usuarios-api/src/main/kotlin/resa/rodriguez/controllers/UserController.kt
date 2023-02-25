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
import resa.rodriguez.mappers.UserMapper
import resa.rodriguez.models.Address
import resa.rodriguez.models.User
import resa.rodriguez.services.*
import java.util.*

private val log = KotlinLogging.logger {}

/**
 * Controlador para el manejo de distintos repositorios
 *
 * @property userMapper
 * @property userRepositoryCached
 * @property addressRepositoryCached
 */
@RestController
@RequestMapping(APIConfig.API_PATH)
class UserController
@Autowired constructor(
    private val userMapper: UserMapper,
    private val service: UserService,
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

            try {

                val userSaved = service.register(userDto)

                ResponseEntity.ok(UserDTOwithToken(userMapper.toDTO(userSaved), jwtTokenUtils.create(userSaved)))
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

            val userSaved = service.create(userDTOcreate)

            ResponseEntity.ok(UserDTOwithToken(userMapper.toDTO(userSaved), jwtTokenUtils.create(userSaved)))
        }

    // El createByAdmin que se usara por parte del cliente es el superior, este simplemente es para la carga de datos inicial
    suspend fun createByAdminInitializer(userDTOcreate: UserDTOcreate): ResponseEntity<UserDTOwithToken> =
        withContext(Dispatchers.IO) {
            log.info { "Creando usuario por parte de un administrador || Carga de datos inicial" }

            val userSaved = service.create(userDTOcreate)

            ResponseEntity.ok(UserDTOwithToken(userMapper.toDTO(userSaved), jwtTokenUtils.create(userSaved)))
        }

    @GetMapping("/login")
    suspend fun login(@Valid @RequestBody userDto: UserDTOlogin): ResponseEntity<UserDTOwithToken> {
        log.info { "Login de usuario: ${userDto.username}" }

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

        return ResponseEntity.ok(UserDTOwithToken(userMapper.toDTO(user), jwtTokenUtils.create(user)))
    }

    // "Find All" Methods
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list")
    suspend fun listUsers(@AuthenticationPrincipal user: User): ResponseEntity<List<UserDTOresponse>> {
        log.info { "Obteniendo listado de usuarios" }

        val res = service.listUsers()
        return ResponseEntity.ok(userMapper.toDTO(res))
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list/paging")
    private suspend fun getAllPaging(
        @AuthenticationPrincipal user: User,
        @RequestParam(defaultValue = APIConfig.PAGINATION_INIT) page: Int = 0,
        @RequestParam(defaultValue = APIConfig.PAGINATION_SIZE) size: Int = 10,
        @RequestParam(defaultValue = APIConfig.PAGINATION_SORT) sortBy: String = "created_at",
    ): ResponseEntity<Page<UserDTOresponse>> = withContext(Dispatchers.IO) {
        log.info { "Buscando usuarios paginados || Pagina: $page" }

        val pageResponse = service.findAllPaging(page, size, sortBy)

        if (pageResponse != null) {
            ResponseEntity.ok(pageResponse)
        } else throw UserExceptionNotFound("Page not found.")
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list/activity/{active}")
    private suspend fun listUsersActive(
        @PathVariable active: Boolean,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<List<UserDTOresponse>> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo listado de usuarios activados" }

        val res = service.findAllByActive(active)

        ResponseEntity.ok(userMapper.toDTO(res))
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

            val addresses = service.findAllFromUserId(user?.id!!).toSet()
            val addr = mutableSetOf<String>()
            addresses.forEach { addr.add(it.address) }
            val result = UserDTOresponse(
                username = user.username,
                email = user.email,
                role = user.role,
                addresses = addr,
                avatar = user.avatar,
                createdAt = user.createdAt,
                active = user.active
            )

            ResponseEntity.ok(result)
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

            val addresses = service.findAllFromUserId(user?.id!!).toSet()
            val addr = mutableSetOf<String>()
            addresses.forEach { addr.add(it.address) }
            val result = UserDTOresponse(
                username = user.username,
                email = user.email,
                role = user.role,
                addresses = addr,
                avatar = user.avatar,
                createdAt = user.createdAt,
                active = user.active
            )

            ResponseEntity.ok(result)
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

            val addresses = service.findAllFromUserId(user?.id!!).toSet()
            val addr = mutableSetOf<String>()
            addresses.forEach { addr.add(it.address) }
            val result = UserDTOresponse(
                username = user.username,
                email = user.email,
                role = user.role,
                addresses = addr,
                avatar = user.avatar,
                createdAt = user.createdAt,
                active = user.active
            )

            ResponseEntity.ok(result)
        }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/phone/{userPhone}")
    suspend fun findByUserPhone(
        @PathVariable userPhone: String,
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con telefono: $userPhone" }

            val user = service.findByUserPhone(userPhone)

            val addresses = service.findAllFromUserId(user?.id!!).toSet()
            val addr = mutableSetOf<String>()
            addresses.forEach { addr.add(it.address) }
            val result = UserDTOresponse(
                username = user.username,
                email = user.email,
                role = user.role,
                addresses = addr,
                avatar = user.avatar,
                createdAt = user.createdAt,
                active = user.active
            )

            ResponseEntity.ok(result)
        }

    // "Update" Methods

    @PutMapping("/me/update")
    private suspend fun updateMySelf(
        @AuthenticationPrincipal user: User,
        @Valid @RequestBody userDTOUpdated: UserDTOUpdated
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        log.info { "Actualizando usuario" }

        val userSaved = service.updateMySelf(user, userDTOUpdated)

        ResponseEntity.ok(userMapper.toDTO(userSaved))
    }

    @PutMapping("/me/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    private suspend fun updateAvatar(
        @AuthenticationPrincipal user: User,
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        val userSaved = service.updateAvatar(user, file)

        ResponseEntity.ok(userMapper.toDTO(userSaved))
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/activity/{email}")
    private suspend fun switchActivityByEmail(
        @PathVariable email: String,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Cambio de actividad por email" }

            val userSaved = service.switchActivity(email)

            ResponseEntity.ok(userMapper.toDTO(userSaved))
        }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/role/{email}")
    private suspend fun updateRoleByEmail(
        @Valid @RequestBody userDTORoleUpdated: UserDTORoleUpdated,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        log.info { "Actualizando rol de usuario con email: ${userDTORoleUpdated.email}" }

        val userSaved = service.updateRoleByEmail(userDTORoleUpdated)

        ResponseEntity.ok(userMapper.toDTO(userSaved))
    }

    // "Delete" Methods
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/delete/{email}")
    private suspend fun deleteUser(
        @PathVariable email: String,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        log.info { "Eliminando al usuario de forma definitiva junto a sus direcciones asociadas" }

        val deleted = service.delete(email)

        ResponseEntity.ok(userMapper.toDTO(deleted))
    }

    // "Me" Method
    @GetMapping("/me")
    suspend fun findMySelf(@AuthenticationPrincipal user: User): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo datos del usuario." }

            ResponseEntity.ok(userMapper.toDTO(user))
        }

    // -- ADDRESSES --

    // "Find All" Methods
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list/address")
    private suspend fun listAddresses(@AuthenticationPrincipal user: User): ResponseEntity<List<Address>> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo listado de direcciones" }

            ResponseEntity.ok(service.findAllAddresses())
        }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list/address/user/{userId}")
    private suspend fun listAddressesByUserId(
        @PathVariable userId: UUID,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo direcciones de usuario con id: $userId" }

        ResponseEntity.ok(service.listAddressesByUserId(userId))
    }

    // "Find One" Methods
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/address/{id}")
    private suspend fun findById(@PathVariable id: UUID, @AuthenticationPrincipal user: User): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo direccion con id: $id" }

            ResponseEntity.ok(service.findAddressById(id))
        }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/address/{name}")
    private suspend fun findByName(
        @PathVariable name: String,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Buscando direccion con nombre: $name" }

            ResponseEntity.ok(service.findAddressByName(name))
        }

    // "Delete" Methods

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/address/{name}")
    private suspend fun deleteAddress(
        @PathVariable name: String,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Eliminando direccion: $name" }

        ResponseEntity.ok(service.deleteAddress(name, user))
    }
}