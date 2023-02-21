package resa.rodriguez.controllers

import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import resa.rodriguez.config.APIConfig
import resa.rodriguez.dto.*
import resa.rodriguez.exceptions.StorageExceptionBadRequest
import resa.rodriguez.mappers.UserMapper
import resa.rodriguez.mappers.fromDTOtoAddresses
import resa.rodriguez.mappers.fromDTOtoUser
import resa.rodriguez.mappers.toAddress
import resa.rodriguez.models.User
import resa.rodriguez.models.UserRole
import resa.rodriguez.repositories.address.AddressRepositoryCached
import resa.rodriguez.repositories.user.UserRepositoryCached
import resa.rodriguez.services.*
import java.util.UUID

private val log = KotlinLogging.logger {}

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    useArrayPolymorphism = true
    encodeDefaults = true
}

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
    private val userRepositoryCached: UserRepositoryCached,
    private val addressRepositoryCached: AddressRepositoryCached,
    private val storageController: StorageController
) {

    // -- GET DEFAULT --
    @GetMapping("")
    private fun bienvenida() = ResponseEntity(
        "Microservicio de gestión de usuarios de una tienda de tecnología para las asignaturas de Acceso a Datos y " +
                "Programación de Procesos y Servicios del IES Luis Vives (Leganés) curso 22/23.",
        HttpStatus.OK
    )

    @GetMapping("/")
    private fun bienvenida2() = ResponseEntity(
        "Microservicio de gestión de usuarios de una tienda de tecnología para las asignaturas de Acceso a Datos y " +
                "Programación de Procesos y Servicios del IES Luis Vives (Leganés) curso 22/23.",
        HttpStatus.OK
    )

    // -- USERS --

    // Register, Create & Login Methods
    @PostMapping("/register")
    private suspend fun register(@Valid @RequestBody userDto: UserDTOregister): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Registro de usuario: ${userDto.username}" }

            try {
                val user = userDto.fromDTOtoUser() ?: return@withContext ResponseEntity(
                    "Password and repeated password does not match.",
                    HttpStatus.BAD_REQUEST
                )

                val userSaved = userRepositoryCached.save(user)

                val addresses = userDto.fromDTOtoAddresses(userSaved.id!!)
                addresses.forEach { addressRepositoryCached.save(it) }

                ResponseEntity.ok(create(userSaved))
            } catch (e: Exception) {
                ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
            }
        }

    @PostMapping("/create")
    private suspend fun create(
        @Valid @RequestBody userDTOcreate: UserDTOcreate,
        @RequestHeader token: String
    ): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Creando usuario por parte de un administrador" }

            val checked = checkToken(token, UserRole.ADMIN)
            if (checked != null) return@withContext checked

            val user = userDTOcreate.fromDTOtoUser()

            val userSaved = userRepositoryCached.save(user)

            val addresses = userDTOcreate.fromDTOtoAddresses(userSaved.id!!)
            addresses.forEach { addressRepositoryCached.save(it) }

            ResponseEntity(create(userSaved), HttpStatus.OK)
        }

    // El createByAdmin que se usara por parte del cliente es el superior, este simplemente es para la carga de datos inicial
    suspend fun createByAdminInitializer(
        @Valid @RequestBody userDTOcreate: UserDTOcreate,
    ): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Creando usuario por parte de un administrador || Carga de datos inicial" }

            val user = userDTOcreate.fromDTOtoUser()

            val userSaved = userRepositoryCached.save(user)

            val addresses = userDTOcreate.fromDTOtoAddresses(userSaved.id!!)
            addresses.forEach { addressRepositoryCached.save(it) }

            ResponseEntity(create(userSaved), HttpStatus.OK)
        }

    @GetMapping("/login")
    private suspend fun login(@Valid @RequestBody userDto: UserDTOlogin): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Login de usuario: ${userDto.email}" }

            try {
                val user = userRepositoryCached.findByEmail(userDto.email)
                    ?: return@withContext ResponseEntity("Incorrect fields.", HttpStatus.NOT_FOUND)

                if (!matches(userDto.password, user.password.encodeToByteArray()))
                    return@withContext ResponseEntity("Incorrect fields.", HttpStatus.NOT_FOUND)
                // Tecnicamente seria una bad request porque las contraseñas no coinciden, pero si lo
                // hacemos asi, el usuario sabria que el email existe y lo que falla es la contraseña,
                // por lo que podria iniciar un ataque de fuerza bruta para suplantar a otro usuario.

                ResponseEntity(create(user), HttpStatus.OK)
            } catch (e: Exception) {
                ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
            }
        }

    // "Find All" Methods
    @GetMapping("/list")
    private suspend fun listUsers(@RequestHeader token: String): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo listado de usuarios" }

        val checked = checkToken(token, UserRole.ADMIN)
        if (checked != null) return@withContext checked

        val res = userRepositoryCached.findAll().toList()
        ResponseEntity(json.encodeToString(userMapper.toDTO(res)), HttpStatus.OK)
    }

    @GetMapping("/list/paging")
    private suspend fun getAllPaging(
        @RequestHeader token: String,
        @RequestParam(defaultValue = APIConfig.PAGINATION_INIT) page: Int = 0,
        @RequestParam(defaultValue = APIConfig.PAGINATION_SIZE) size: Int = 10,
        @RequestParam(defaultValue = APIConfig.PAGINATION_SORT) sortBy: String = "created_at",
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Buscando usuarios paginados || Pagina: $page" }

        val checked = checkToken(token, UserRole.ADMIN)
        if (checked != null) return@withContext checked

        val pageRequest = PageRequest.of(page, size, Sort.Direction.ASC, sortBy)
        val pageResponse = userRepositoryCached.findAllPaged(pageRequest).firstOrNull()?.toList()

        if (pageResponse != null) {
            ResponseEntity(json.encodeToString(userMapper.toDTO(pageResponse)), HttpStatus.OK)
        } else ResponseEntity("Page not found", HttpStatus.NOT_FOUND)
    }

    @GetMapping("/list/activity/{active}")
    private suspend fun listUsersActive(
        @PathVariable active: Boolean,
        @RequestHeader token: String
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo listado de usuarios activados" }

        val checked = checkToken(token, UserRole.ADMIN)
        if (checked != null) return@withContext checked

        val res = userRepositoryCached.findByActivo(active).toList()

        ResponseEntity.ok(json.encodeToString(userMapper.toDTO(res)))
    }

    // "Find One" Methods
    @GetMapping("/username/{username}")
    private suspend fun findByUsername(
        @PathVariable username: String,
        @RequestHeader token: String
    ): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con username: $username" }

            val checked = checkToken(token, UserRole.ADMIN)
            if (checked != null) return@withContext checked

            val user = userRepositoryCached.findByUsername(username)
                ?: return@withContext ResponseEntity("User with name: $username not found.", HttpStatus.NOT_FOUND)

            val addresses = addressRepositoryCached.findAllFromUserId(user.id!!).toSet()
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

            ResponseEntity(json.encodeToString(result), HttpStatus.OK)
        }

    @GetMapping("/id/{userId}")
    private suspend fun findByUserId(
        @PathVariable userId: UUID,
        @RequestHeader token: String
    ): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con id: $userId" }

            val checked = checkToken(token, UserRole.ADMIN)
            if (checked != null) return@withContext checked

            val user = userRepositoryCached.findById(userId)
                ?: return@withContext ResponseEntity("User with id: $userId not found", HttpStatus.NOT_FOUND)

            val addresses = addressRepositoryCached.findAllFromUserId(user.id!!).toSet()
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

            ResponseEntity(json.encodeToString(result), HttpStatus.OK)
        }

    @GetMapping("/email/{userEmail}")
    private suspend fun findByUserEmail(
        @PathVariable userEmail: String,
        @RequestHeader token: String
    ): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con email: $userEmail" }

            val checked = checkToken(token, UserRole.ADMIN)
            if (checked != null) return@withContext checked

            val user = userRepositoryCached.findByEmail(userEmail)
                ?: return@withContext ResponseEntity("User with email: $userEmail not found", HttpStatus.NOT_FOUND)

            val addresses = addressRepositoryCached.findAllFromUserId(user.id!!).toSet()
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

            ResponseEntity(json.encodeToString(result), HttpStatus.OK)
        }

    @GetMapping("/phone/{userPhone}")
    private suspend fun findByUserPhone(
        @PathVariable userPhone: String,
        @RequestHeader token: String
    ): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con telefono: $userPhone" }

            val checked = checkToken(token, UserRole.ADMIN)
            if (checked != null) return@withContext checked

            val user = userRepositoryCached.findByPhone(userPhone)
                ?: return@withContext ResponseEntity("User with phone: $userPhone not found", HttpStatus.NOT_FOUND)

            val addresses = addressRepositoryCached.findAllFromUserId(user.id!!).toSet()
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

            ResponseEntity(json.encodeToString(result), HttpStatus.OK)
        }

    // "Update" Methods

    @PutMapping("/me/update")
    private suspend fun updateMySelf(
        @RequestHeader token: String,
        @Valid @RequestBody userDTOUpdated: UserDTOUpdated
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Actualizando usuario" }

        val user = getUserFromToken(token, userRepositoryCached)
            ?: return@withContext ResponseEntity("User not found", HttpStatus.NOT_FOUND)

        val updatedPassword = if (userDTOUpdated.password.isBlank()) user.password
        else cipher(userDTOUpdated.password)

        if (userDTOUpdated.addresses.isNotEmpty()) {
            val addresses = mutableSetOf<String>()
            addresses.addAll(userDTOUpdated.addresses)
            addressRepositoryCached.findAllFromUserId(user.id!!).toSet().forEach { addresses.add(it.address) }
            addressRepositoryCached.deleteAllByUserId(user.id)

            addresses.forEach { addressRepositoryCached.save(toAddress(user.id, it)) }
        }

        val userUpdated = User(
            id = user.id,
            username = user.username,
            email = user.email,
            password = updatedPassword,
            phone = user.phone,
            avatar = user.avatar,
            role = user.role,
            createdAt = user.createdAt,
            active = user.active
        )

        val userSaved = userRepositoryCached.save(userUpdated)

        return@withContext ResponseEntity(json.encodeToString(userMapper.toDTO(userSaved)), HttpStatus.OK)
    }

    @PutMapping("/activity/{email}")
    private suspend fun switchActivityByEmail(
        @PathVariable email: String,
        @RequestHeader token: String
    ): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Cambio de actividad por email" }

            val checked = checkToken(token, UserRole.ADMIN)
            if (checked != null) return@withContext checked

            val user = userRepositoryCached.findByEmail(email)
                ?: return@withContext ResponseEntity("User with email: $email not found", HttpStatus.NOT_FOUND)

            val userUpdateActivity = User(
                id = user.id,
                username = user.username,
                email = user.email,
                password = user.password,
                phone = user.phone,
                avatar = user.avatar,
                role = user.role,
                createdAt = user.createdAt,
                active = !user.active
            )

            val userSaved = userRepositoryCached.save(userUpdateActivity)

            ResponseEntity(json.encodeToString(userMapper.toDTO(userSaved)), HttpStatus.OK)
        }

    @PutMapping("/role/{email}")
    private suspend fun updateRoleByEmail(
        @Valid @RequestBody userDTORoleUpdated: UserDTORoleUpdated,
        @RequestHeader token: String
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Actualizando rol de usuario con email: ${userDTORoleUpdated.email}" }

        val checked = checkToken(token, UserRole.SUPER_ADMIN)
        if (checked != null) return@withContext checked

        val user = userRepositoryCached.findByEmail(userDTORoleUpdated.email)
            ?: return@withContext ResponseEntity(
                "User with email: ${userDTORoleUpdated.email} not found",
                HttpStatus.NOT_FOUND
            )

        val updatedRole =
            if (userDTORoleUpdated.role.name.uppercase() != (UserRole.USER.name) ||
                userDTORoleUpdated.role.name.uppercase() != (UserRole.ADMIN.name) ||
                userDTORoleUpdated.role.name.uppercase() != (UserRole.SUPER_ADMIN.name)
            ) {
                user.role
            } else userDTORoleUpdated.role

        val userUpdated = User(
            id = user.id,
            username = user.username,
            email = user.email,
            password = user.password,
            phone = user.phone,
            avatar = user.avatar,
            role = updatedRole,
            createdAt = user.createdAt,
            active = user.active
        )

        val userSaved = userRepositoryCached.save(userUpdated)

        ResponseEntity(json.encodeToString(userMapper.toDTO(userSaved)), HttpStatus.OK)
    }

    // "Delete" Methods
    @DeleteMapping("/delete/{email}")
    private suspend fun deleteUser(@PathVariable email: String, @RequestHeader token: String): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Eliminando al usuario de forma definitiva junto a sus direcciones asociadas" }

            val checked = checkToken(token, UserRole.SUPER_ADMIN)
            if (checked != null) return@withContext checked

            val user = userRepositoryCached.findByEmail(email)
                ?: return@withContext ResponseEntity("User with email: $email not found", HttpStatus.NOT_FOUND)

            addressRepositoryCached.deleteAllByUserId(user.id!!)

            userRepositoryCached.deleteById(user.id)

            ResponseEntity("User with email: $email deleted successfully", HttpStatus.OK)
        }

    // "Me" Method
    @GetMapping("/me")
    private suspend fun findMySelf(@RequestHeader token: String): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo datos del usuario." }

        val user = getUserDTOFromToken(token, userRepositoryCached, userMapper)
        if (user == null) ResponseEntity("User not found", HttpStatus.NOT_FOUND)
        else ResponseEntity(json.encodeToString(user), HttpStatus.OK)
    }

    // -- ADDRESSES --

    // "Find All" Methods
    @GetMapping("/list/address")
    private suspend fun listAddresses(@RequestHeader token: String): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo listado de direcciones" }

            val checked = checkToken(token, UserRole.ADMIN)
            if (checked != null) return@withContext checked

            ResponseEntity(json.encodeToString(addressRepositoryCached.findAll().toList()), HttpStatus.OK)
        }

    @GetMapping("/list/address/user/{userId}")
    private suspend fun listAddressesByUserId(
        @PathVariable userId: UUID,
        @RequestHeader token: String
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo direcciones de usuario con id: $userId" }

        val checked = checkToken(token, UserRole.ADMIN)
        if (checked != null) return@withContext checked

        val address = addressRepositoryCached.findAllFromUserId(userId).toList()

        if (address.isEmpty()) ResponseEntity(
            "Addresses with userId: $userId not found",
            HttpStatus.NOT_FOUND
        ) else ResponseEntity(json.encodeToString(address), HttpStatus.OK)
    }

    // "Find One" Methods
    @GetMapping("/address/{id}")
    private suspend fun findById(@PathVariable id: UUID, @RequestHeader token: String): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo direccion con id: $id" }

            val checked = checkToken(token, UserRole.ADMIN)
            if (checked != null) return@withContext checked

            val address = addressRepositoryCached.findById(id)
                ?: return@withContext ResponseEntity("Address with id: $id not found", HttpStatus.NOT_FOUND)

            ResponseEntity(address.address, HttpStatus.OK)
        }

    @GetMapping("/address/{name}")
    private suspend fun findByName(@PathVariable name: String, @RequestHeader token: String): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Buscando direccion con nombre: $name" }

            val checked = checkToken(token, UserRole.ADMIN)
            if (checked != null) return@withContext checked

            val address = addressRepositoryCached.findAllByAddress(name).firstOrNull()
                ?: return@withContext ResponseEntity("Address with name: $name not found", HttpStatus.NOT_FOUND)

            ResponseEntity(address.address, HttpStatus.OK)
        }

    // "Delete" Methods

    @DeleteMapping("/address/{name}")
    private suspend fun deleteAddress(
        @PathVariable name: String,
        @RequestHeader token: String
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Eliminando direccion: $" }

        val userDto = getUserDTOFromToken(token, userRepositoryCached, userMapper)
            ?: return@withContext ResponseEntity("User not found", HttpStatus.NOT_FOUND)

        val address = addressRepositoryCached.findAllByAddress(name).firstOrNull()
        val user = userRepositoryCached.findByEmail(userDto.email)

        if (address == null) return@withContext ResponseEntity("Address not found", HttpStatus.NOT_FOUND)
        if (user == null) return@withContext ResponseEntity("User not found", HttpStatus.NOT_FOUND)

        val addresses = addressRepositoryCached.findAllFromUserId(user.id!!).toSet()

        if (address.userId == user.id && addresses.size > 1) {
            addressRepositoryCached.deleteById(address.id!!)
        } else return@withContext ResponseEntity("No ha sido posible eliminar la direccion", HttpStatus.BAD_REQUEST)

        return@withContext ResponseEntity("Direccion eliminada", HttpStatus.OK)
    }

    @PutMapping("/me/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    private suspend fun updateAvatar(@RequestHeader token: String, @RequestPart("file") file: MultipartFile): ResponseEntity<out Any> = withContext(Dispatchers.IO) {
        val user = getUserFromToken(token, userRepositoryCached)
            ?: return@withContext ResponseEntity("User not found", HttpStatus.NOT_FOUND)

        val response = storageController.uploadFile(file)
        if (!response.statusCode.is2xxSuccessful) return@withContext response
        val avatarUrl = response.body?.get("url")
            ?: return@withContext ResponseEntity("Url not found.", HttpStatus.NOT_FOUND)

        val userUpdated = User(
            id = user.id,
            username = user.username,
            email = user.email,
            password = user.password,
            phone = user.phone,
            avatar = avatarUrl,
            role = user.role,
            createdAt = user.createdAt,
            active = user.active
        )

        val userSaved = userRepositoryCached.save(userUpdated)

        ResponseEntity(json.encodeToString(userMapper.toDTO(userSaved)), HttpStatus.OK)
    }
}