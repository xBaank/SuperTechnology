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
import resa.rodriguez.exceptions.UserExceptionNotFound
import resa.rodriguez.exceptions.AddressExceptionNotFound
import resa.rodriguez.exceptions.AddressExceptionBadRequest
import resa.rodriguez.exceptions.StorageExceptionBadRequest
import resa.rodriguez.exceptions.StorageExceptionNotFound
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
 * Controller that will manage every endpoint related to users
 * by calling the necessary services and utility classes.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
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
    /**
     * Endpoint that will return a response entity with a welcome message.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @return Welcome message.
     */
    @Operation(summary = "Bienvenida 1", description = "Metodo que devuelve un mensaje de bienvenida.", tags = ["USER"])
    @ApiResponse(responseCode = "200", description = "Mensaje de bienvenida.")
    @GetMapping("")
    fun bienvenida() = ResponseEntity(
        "Micro servicio de gestión de usuarios de una tienda de tecnología para las asignaturas de Acceso a Datos y " +
                "Programación de Procesos y Servicios del IES Luis Vives (Leganés) curso 22/23.",
        HttpStatus.OK
    )

    /**
     * Endpoint that will return a response entity with a welcome message.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @return Welcome message.
     */
    @Operation(summary = "Bienvenida 2", description = "Metodo que devuelve un mensaje de bienvenida.", tags = ["USER"])
    @ApiResponse(responseCode = "200", description = "Mensaje de bienvenida.")
    @GetMapping("/")
    fun bienvenida2() = ResponseEntity(
        "Micro servicio de gestión de usuarios de una tienda de tecnología para las asignaturas de Acceso a Datos y " +
                "Programación de Procesos y Servicios del IES Luis Vives (Leganés) curso 22/23.",
        HttpStatus.OK
    )

    /**
     * Endpoint for registering.
     * It will return a response entity with the registered user's DTO and its token.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param userDto Valid user DTO for registering.
     * @return Response Entity with the DTO for visualization from the registered user, along with its JWT token.
     * @throws UserExceptionBadRequest if it cannot register successfully.
     */
    @Operation(summary = "Register", description = "Endpoint for registering.", tags = ["USER"])
    @Parameter(name = "userDTO", description = "Valid user DTO for registering.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with the DTO for visualization from the registered user, along with its JWT token.")
    @ApiResponse(responseCode = "400", description = "If it cannot register successfully.")
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

    /**
     * Endpoint for creating.
     * It will return a response entity with the created user's DTO and its token.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param userDTOcreate Valid DTO for creation.
     * @param user Token for authentication.
     * @return Response Entity with the DTO for visualization from the created user, along with its JWT token.
     * @throws UserExceptionBadRequest if it cannot create successfully.
     */
    @Operation(summary = "Create", description = "Endpoint for creating users that can only be accessed by administrators.", tags = ["USER"])
    @Parameter(name = "userDTOcreate", description = "Valid DTO for creation.", required = true)
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with the DTO for visualization from the created user, along with its JWT token.")
    @ApiResponse(responseCode = "400", description = "If it cannot create successfully.")
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

    /**
     * Function for the initial data load.
     * It will return a response entity with the created user's DTO and its token.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param userDTOcreate Valid DTO for creation.
     * @return Response Entity with the DTO for visualization from the created user, along with its JWT token.
     * @throws UserExceptionBadRequest if it cannot create successfully.
    */
    suspend fun createByAdminInitializer(userDTOcreate: UserDTOcreate): ResponseEntity<UserDTOwithToken> =
        withContext(Dispatchers.IO) {
            log.info { "Creando usuario por parte de un administrador || Carga de datos inicial" }

            userDTOcreate.validate()
            val userSaved = service.create(userDTOcreate)
            val addresses = userSaved.id?.let { service.findAllFromUserId(it).toSet() } ?: setOf()

            ResponseEntity(UserDTOwithToken(userSaved.toDTO(addresses), jwtTokenUtils.create(userSaved)), HttpStatus.CREATED)
        }

    /**
     * Endpoint for login.
     * It will return a response entity with the logged-in user's DTO and its token.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param userDto Valid DTO for logging in.
     * @return Response Entity with the DTO for visualization from the logged-in user, along with its JWT token.
     * @throws UserExceptionBadRequest if it cannot log in successfully.
     */
    @Operation(summary = "Login", description = "Endpoin for login.", tags = ["USER"])
    @Parameter(name = "userDTOlogin", description = "Valid DTO for logging in.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with the DTO for visualization from the logged-in user, along with its JWT token.")
    @ApiResponse(responseCode = "400", description = "If it cannot log in successfully.")
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
        // Now we authenticate the user
        SecurityContextHolder.getContext().authentication = authentication

        // And return the authenticated user
        val user = authentication.principal as User
        val addresses = user.id?.let { service.findAllFromUserId(it).toSet() } ?: setOf()

        return ResponseEntity.ok(UserDTOwithToken(user.toDTO(addresses), jwtTokenUtils.create(user)))
    }

    /**
     * Endpoint for finding all users.
     * It will return a response entity with a list of all users mapped to their corresponding DTOs.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param user Token for authentication.
     * @return Response Entity with a list of all users mapped to their corresponding DTOs.
     */
    @Operation(summary = "List users", description = "Endpoint for finding all users.", tags = ["USER"])
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a list of all users mapped to their corresponding DTOs.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list")
    suspend fun listUsers(@AuthenticationPrincipal user: User): ResponseEntity<List<UserDTOresponse>> {
        log.info { "Obteniendo listado de usuarios" }

        val res = service.listUsers()
        return ResponseEntity.ok(res.toDTOlist(aRepo))
    }

    /**
     * Endpoint for finding all users in a paginated way.
     * It will return a response entity with a page containing users mapped to their corresponding DTOs.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param user Token for authentication.
     * @param page Page number to be searched.
     * @param size Size of the page.
     * @param sortBy How we want to sort the contents of the page.
     * @return Response Entity with a page containing users mapped to their corresponding DTOs.
     * @throws UserExceptionNotFound When a page is not found.
     */
    @Operation(summary = "Find All Paging", description = "Endpoint for finding all users in a paginated way.", tags = ["USER"])
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @Parameter(name = "page", description = "Page number to be searched.", required = false)
    @Parameter(name = "size", description = "Size of the page.", required = false)
    @Parameter(name = "sortBy", description = "How we want to sort the contents of the page.", required = false)
    @ApiResponse(responseCode = "200", description = "Response Entity with a page containing users mapped to their corresponding DTOs.")
    @ApiResponse(responseCode = "404", description = "When a page is not found.")
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

    /**
     * Endpoint for finding all users that are currently in the specified activity.
     * It will return a response entity with a list containing users mapped to
     * their corresponding DTOs and filtered by their activity.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param active Activity for filtering.
     * @param user Token for authentication.
     * @return Response Entity with a list containing users mapped to their corresponding DTOs and filtered by their activity.
     */
    @Operation(summary = "List Users Active", description = "Endpoint for finding all users that are currently in the specified activity.", tags = ["USER"])
    @Parameter(name = "active", description = "Activity for filtering.", required = true)
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a list containing users mapped to their corresponding DTOs and filtered by their activity.")
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

    /**
     * Endpoint for finding a user whose username matches the one specified.
     * It will return a response entity with a user mapped to their corresponding DTO.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param u Token for authentication.
     * @param username Username to be searched.
     * @return Response Entity with a user mapped to their corresponding DTO.
     * @throws UserExceptionNotFound When the user was not found.
     */
    @Operation(summary = "Find By Username", description = "Endpoint for finding a user whose username matches the one specified.", tags = ["USER"])
    @Parameter(name = "u", description = "Token for authentication.", required = true)
    @Parameter(name = "username", description = "Username to be searched.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a user mapped to their corresponding DTO.")
    @ApiResponse(responseCode = "404", description = "When the user was not found.")
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

    /**
     * Endpoint for finding a user whose id matches the one specified.
     * It will return a response entity with a user mapped to their corresponding DTO.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param u Token for authentication.
     * @param userId UUID to be searched.
     * @return Response Entity with a user mapped to their corresponding DTO.
     * @throws UserExceptionNotFound When the user was not found.
     */
    @Operation(summary = "Find By Id", description = "Endpoint for finding a user whose id matches the one specified.", tags = ["USER"])
    @Parameter(name = "u", description = "Token for authentication.", required = true)
    @Parameter(name = "userId", description = "UUID to be searched.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a user mapped to their corresponding DTO.")
    @ApiResponse(responseCode = "404", description = "When the user was not found.")
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

    /**
     * Endpoint for finding a user whose email matches the one specified.
     * It will return a response entity with a user mapped to their corresponding DTO.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param u Token for authentication.
     * @param userEmail Email to be searched.
     * @return Response Entity with a user mapped to their corresponding DTO.
     * @throws UserExceptionNotFound When the user was not found.
     */
    @Operation(summary = "Find By Email", description = "Endpoint for finding a user whose email matches the one specified.", tags = ["USER"])
    @Parameter(name = "u", description = "Token for authentication.", required = true)
    @Parameter(name = "userEmail", description = "Email to be searched.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a user mapped to their corresponding DTO.")
    @ApiResponse(responseCode = "404", description = "When the user was not found.")
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

    /**
     * Endpoint for finding a user whose phone number matches the one specified.
     * It will return a response entity with a user mapped to their corresponding DTO.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param u Token for authentication.
     * @param userPhone Phone number to be searched.
     * @return Response Entity with a user mapped to their corresponding DTO.
     * @throws UserExceptionNotFound When the user was not found.
     */
    @Operation(summary = "Find By Phone", description = "Endpoint for finding a user whose phone number matches the one specified.", tags = ["USER"])
    @Parameter(name = "u", description = "Token for authentication.", required = true)
    @Parameter(name = "userPhone", description = "Phone number to be searched.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a user mapped to their corresponding DTO.")
    @ApiResponse(responseCode = "404", description = "When the user was not found.")
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

    /**
     * Endpoint for updating your own password and/or list of addresses.
     * It will return a response entity with a user mapped to their corresponding DTO.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param user Token for authentication.
     * @param userDTOUpdated Valid DTO containing the new information.
     * @return Response Entity with the updated user mapped to their corresponding DTO.
     * @throws UserExceptionBadRequest When the DTO is invalid.
     */
    @Operation(summary = "Update Myself", description = "Endpoint for updating your own password and/or list of addresses.", tags = ["USER"])
    @Parameter(name = "u", description = "Token for authentication.", required = true)
    @Parameter(name = "userDTOUpdated", description = "Valid DTO containing the new information.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with the updated user mapped to their corresponding DTO.")
    @ApiResponse(responseCode = "400", description = "When the DTO is invalid.")
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

    /**
     * Endpoint for updating your own avatar.
     * It will return a response entity with a user mapped to their corresponding DTO.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param user Token for authentication.
     * @param file New avatar.
     * @return Response Entity with the updated user mapped to their corresponding DTO.
     * @throws StorageExceptionBadRequest When the file cannot be saved or the file type cannot be determined.
     * @throws StorageExceptionNotFound When the file cannot be read or is not found.
     */
    @Operation(summary = "Update Avatar", description = "Endpoint for updating your own avatar.", tags = ["USER"])
    @Parameter(name = "u", description = "Token for authentication.", required = true)
    @Parameter(name = "file", description = "New avatar.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with the updated user mapped to their corresponding DTO.")
    @ApiResponse(responseCode = "400", description = "When the file cannot be saved or the file type cannot be determined.")
    @ApiResponse(responseCode = "404", description = "When the file cannot be read or is not found.")
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

    /**
     * Endpoint for switching a user's activity by their email address.
     * It will return a response entity with a user mapped to their corresponding DTO.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param email Email to be searched.
     * @param user Token for authentication.
     * @return Response Entity with the updated user mapped to their corresponding DTO.
     * @throws UserExceptionNotFound When the user was not found.
     */
    @Operation(summary = "Switch Activity By Email", description = "Endpoint for switching a user's activity by their email address.", tags = ["USER"])
    @Parameter(name = "email", description = "Email to be searched.", required = true)
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with the updated user mapped to their corresponding DTO.")
    @ApiResponse(responseCode = "404", description = "When the user was not found.")
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

    /**
     * Endpoint for updating a user's role by their email address.
     * It will return a response entity with a user mapped to their corresponding DTO.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param userDTORoleUpdated DTO with the user's email and the new role.
     * @param user Token for authentication.
     * @return Response Entity with the updated user mapped to their corresponding DTO.
     * @throws UserExceptionBadRequest When the DTO is invalid.
     * @throws UserExceptionNotFound When the user was not found.
     */
    @Operation(summary = "Update Role By Email", description = "Endpoint for updating a user's role by their email address.", tags = ["USER"])
    @Parameter(name = "userDTORoleUpdated", description = "DTO with the user's email and the new role.", required = true)
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with the updated user mapped to their corresponding DTO.")
    @ApiResponse(responseCode = "400", description = "When the DTO is invalid.")
    @ApiResponse(responseCode = "404", description = "When the user was not found.")
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

    /**
     * Endpoint for deleting a user by their email address.
     * It will return a response entity with the deleted user mapped to their corresponding DTO.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param email Email to be searched.
     * @param user Token for authentication.
     * @return Response Entity with the deleted user mapped to their corresponding DTO.
     * @throws UserExceptionNotFound When the user was not found.
     */
    @Operation(summary = "Delete User", description = "Endpoint for deleting a user by their email address.", tags = ["USER"])
    @Parameter(name = "email", description = "Email to be searched.", required = true)
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with the deleted user mapped to their corresponding DTO.")
    @ApiResponse(responseCode = "404", description = "When the user was not found.")
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

    /**
     * Endpoint for finding oneself.
     * It will return a response entity with the user mapped to their corresponding DTO.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param user Token for authentication.
     * @return Response Entity with the user mapped to their corresponding DTO.
     */
    @Operation(summary = "Find Myself", description = "Endpoint for finding oneself.", tags = ["USER"])
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a user mapped to their corresponding DTO.")
    @GetMapping("/me")
    suspend fun findMySelf(@AuthenticationPrincipal user: User): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo datos del usuario." }
            val addresses = user.id?.let { service.findAllFromUserId(it).toSet() } ?: setOf()

            ResponseEntity.ok(user.toDTO(addresses))
        }

    /**
     * Endpoint for getting all addresses.
     * It will return a response entity with a list of all addresses.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param user Token for authentication.
     * @return Response Entity with a list of all addresses.
     */
    @Operation(summary = "List Addresses", description = "Endpoint for getting all addresses.", tags = ["ADDRESS"])
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a list of all addresses.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list/address")
    suspend fun listAddresses(@AuthenticationPrincipal user: User): ResponseEntity<List<Address>> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo listado de direcciones" }

            ResponseEntity.ok(service.findAllAddresses())
        }

    /**
     * Endpoint for finding all addresses in a paginated way.
     * It will return a response entity with a page containing addresses.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param user Token for authentication.
     * @param page Page number to be searched.
     * @param size Size of the page.
     * @param sortBy How we want to sort the contents of the page.
     * @return Response Entity with a page containing addresses.
     * @throws AddressExceptionNotFound When a page is not found.
     */
    @Operation(summary = "List Addresses Paginadas", description = "Endpoint for finding all addresses in a paginated way.", tags = ["ADDRESS"])
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @Parameter(name = "page", description = "Page number to be searched.", required = false)
    @Parameter(name = "size", description = "Size of the page.", required = false)
    @Parameter(name = "sortBy", description = "How we want to sort the contents of the page.", required = false)
    @ApiResponse(responseCode = "200", description = "Response Entity with a page containing addresses.")
    @ApiResponse(responseCode = "404", description = "When a page is not found.")
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

    /**
     * Endpoint for finding all addresses from a particular user.
     * It will return a response entity with a string containing all addresses
     * from the user with the given id,and separated by commas.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param userId UUID from the user whose addresses we want to find.
     * @param user Token for authentication.
     * @return Response Entity with a string containing all addresses
     * from the user with the given id, separated by commas.
     * @throws AddressExceptionNotFound When no address is found.
     */
    @Operation(summary = "List Addresses By User ID", description = "Endpoint for finding all addresses from a particular user.", tags = ["ADDRESS"])
    @Parameter(name = "userId", description = "UUID from the user whose addresses we want to find.", required = true)
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a string containing all addresses from the user with the given id, separated by commas.")
    @ApiResponse(responseCode = "404", description = "When no address is found.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/list/address/{userId}")
    suspend fun listAddressesByUserId(
        @PathVariable userId: UUID,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo direcciones de usuario con id: $userId" }

        ResponseEntity.ok(service.listAddressesByUserId(userId))
    }

    /**
     * Endpoint for finding an address from a given id.
     * It will return a response entity with a string that is the name of the found address.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param id UUID to be searched.
     * @param user Token for authentication.
     * @return Response Entity with a string that is the name of the found address.
     * @throws AddressExceptionNotFound When no address is found.
     */
    @Operation(summary = "Find By ID", description = "Endpoint for finding an address from a given id.", tags = ["ADDRESS"])
    @Parameter(name = "id", description = "UUID to be searched.", required = true)
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a string that is the name of the found address.")
    @ApiResponse(responseCode = "404", description = "When no address is found.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/address/{id}")
    suspend fun findById(@PathVariable id: UUID, @AuthenticationPrincipal user: User): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo direccion con id: $id" }

            ResponseEntity.ok(service.findAddressById(id))
        }

    /**
     * Endpoint for finding an address from a given name.
     * It will return a response entity with a string that is the name of the found address.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param name Address to be searched.
     * @param user Token for authentication.
     * @return Response Entity with a string that is the name of the found address.
     * @throws AddressExceptionNotFound When no address is found.
     */
    @Operation(summary = "Find By Name", description = "Endpoint for finding an address from a given name.", tags = ["ADDRESS"])
    @Parameter(name = "name", description = "Address to be searched.", required = false)
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a string that is the name of the found address.")
    @ApiResponse(responseCode = "404", description = "When no address is found.")
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

    /**
     * Endpoint for deleting an address from a given name and from one's list of addresses.
     * It will return a response entity with a string that is the name of the found address.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param name Address to be searched.
     * @param user Token for authentication.
     * @return Response Entity with a string that is the name of the deleted address.
     * @throws AddressExceptionNotFound When no address is found.
     * @throws UserExceptionNotFound When no user with that email is found.
     * @throws AddressExceptionBadRequest When the address could not be deleted.
     */
    @Operation(summary = "Delete My Address", description = "Endpoint for deleting an address from a given name and from one's list of addresses.", tags = ["ADDRESS"])
    @Parameter(name = "name", description = "Address to be searched.", required = false)
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a string that is the name of the deleted address.")
    @ApiResponse(responseCode = "400", description = "When the address could not be deleted.")
    @ApiResponse(responseCode = "404", description = "When no address is found or no user with that email is found.")
    @DeleteMapping("/me/address")
    suspend fun deleteMyAddress(
        @RequestParam(defaultValue = "") name: String = "",
        @AuthenticationPrincipal user: User
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Eliminando direccion: $name" }

        ResponseEntity.ok(service.deleteAddress(name, user.email))
    }

    /**
     * Endpoint for deleting an address from a given name and from a particular user, found by their email.
     * It will return a response entity with a string that is the name of the found address.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param name Address to be searched.
     * @param name Email to be searched.
     * @param user Token for authentication.
     * @return Response Entity with a string that is the name of the deleted address.
     * @throws AddressExceptionNotFound When no address is found.
     * @throws UserExceptionNotFound When no user with that email is found.
     * @throws AddressExceptionBadRequest When the address could not be deleted.
     */
    @Operation(summary = "Delete Address", description = "Endpoint for deleting an address from a given name and from a particular user, found by their email.", tags = ["ADDRESS"])
    @Parameter(name = "name", description = "Address to be searched.", required = false)
    @Parameter(name = "email", description = "Email to be searched.", required = false)
    @Parameter(name = "user", description = "Token for authentication.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a string that is the name of the found address.")
    @ApiResponse(responseCode = "400", description = "When the address could not be deleted.")
    @ApiResponse(responseCode = "404", description = "When no address is found or no user with that email is found.")
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