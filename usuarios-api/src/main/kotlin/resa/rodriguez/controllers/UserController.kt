package resa.rodriguez.controllers

import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.withContext
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import resa.rodriguez.config.APIConfig
import resa.rodriguez.dto.*
import resa.rodriguez.mappers.UserMapper
import resa.rodriguez.mappers.fromDTOtoAddresses
import resa.rodriguez.mappers.fromDTOtoUser
import resa.rodriguez.models.UserRole
import resa.rodriguez.repositories.address.AddressRepositoryCached
import resa.rodriguez.repositories.user.UserRepositoryCached
import resa.rodriguez.services.checkToken
import resa.rodriguez.services.create
import resa.rodriguez.services.getUserFromToken
import resa.rodriguez.services.matches
import java.util.UUID

private val log = KotlinLogging.logger {}

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    useArrayPolymorphism = true
    encodeDefaults = true
    serializersModule = SerializersModule {
        polymorphic(Any::class) {
            subclass(String::class, String.serializer())
            subclass(UserDTOresponse::class)
            subclass(UserDTOlogin::class)
            subclass(UserDTOregister::class)
            subclass(UserDTOcreate::class)
            subclass(List::class, ListSerializer(PolymorphicSerializer(Any::class).nullable))
        }
    }
}
/**
 * Controlador para el manejo de distintos repositorios
 *
 * @property userMapper
 * @property userRepositoryCached
 * @property addressRepositoryCached
 */
@RestController
@RequestMapping(APIConfig.API_PATH + "/users")
class UserController
@Autowired constructor(
    private val userMapper: UserMapper,
    private val userRepositoryCached: UserRepositoryCached,
    private val addressRepositoryCached: AddressRepositoryCached
) {

    // -- USERS --

    // Register & Login Methods
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

                ResponseEntity.ok(create(user))
            } catch (e: Exception) {
                ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
            }
        }

    // "Find All" Methods
    @GetMapping("/list")
    private suspend fun listUsers(@RequestHeader token: String): ResponseEntity<out Any> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo listado de usuarios" }

        val checked = checkToken(token, UserRole.ADMIN)
        if (checked != null) return@withContext checked

        val res = userRepositoryCached.findAll().toList()
        ResponseEntity.ok(userMapper.toDTO(res))
    }

    @GetMapping("/list/activity/{active}")
    private suspend fun listUsersActive(@PathVariable active: Boolean, @RequestHeader token: String): ResponseEntity<out Any> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo listado de usuarios activados" }

        val checked = checkToken(token, UserRole.ADMIN)
        if (checked != null) return@withContext checked

        val res = userRepositoryCached.findByActivo(active).toList()

        ResponseEntity.ok(userMapper.toDTO(res))
    }

    // "Find One" Methods
    @GetMapping("/username/{username}")
    private suspend fun findByUsername(@PathVariable username: String, @RequestHeader token: String): ResponseEntity<out Any> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con username: $username" }

            val checked = checkToken(token, UserRole.ADMIN)
            if (checked != null) return@withContext checked

            val user = userRepositoryCached.findByUsername(username).firstOrNull()
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

            ResponseEntity.ok(result)
        }

    @GetMapping("/id/{userId}")
    private suspend fun findByUserId(@PathVariable userId: UUID, @RequestHeader token: String): ResponseEntity<out Any> =
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

            ResponseEntity.ok(result)
        }

    @GetMapping("/email/{userEmail}")
    private suspend fun findByUserEmail(@PathVariable userEmail: String, @RequestHeader token: String): ResponseEntity<out Any> =
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

            ResponseEntity.ok(result)
        }

    @GetMapping("/phone/{userPhone}")
    private suspend fun findByUserPhone(@PathVariable userPhone: String, @RequestHeader token: String): ResponseEntity<out Any> =
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

            ResponseEntity.ok(result)
        }

    // -- ADDRESSES --

    // "Find All" Methods
    @GetMapping("/list/address")
    private suspend fun listAddresses(@RequestHeader token: String): ResponseEntity<out Any> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo listado de direcciones" }

        val checked = checkToken(token, UserRole.ADMIN)
        if (checked != null) return@withContext checked

        ResponseEntity.ok(addressRepositoryCached.findAll().toList())
    }

    @GetMapping("/list/address/user/{userId}")
    private suspend fun listAddressesByUserId(@PathVariable userId: UUID, @RequestHeader token: String): ResponseEntity<out Any> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo direcciones de usuario con id: $userId" }

        val checked = checkToken(token, UserRole.ADMIN)
        if (checked != null) return@withContext checked

        val address = addressRepositoryCached.findAllFromUserId(userId).toList()

        if (address.isEmpty()) ResponseEntity(
            "Addresses with userId: $userId not found",
            HttpStatus.NOT_FOUND
        ) else ResponseEntity.ok(address)
    }

    // "Find One" Methods
    @GetMapping("/address/{id}")
    private suspend fun findById(@PathVariable id: UUID, @RequestHeader token: String): ResponseEntity<out Any> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo direccion con id: $id" }

        val checked = checkToken(token, UserRole.ADMIN)
        if (checked != null) return@withContext checked

        val address = addressRepositoryCached.findById(id)
            ?: return@withContext ResponseEntity("Address with id: $id not found", HttpStatus.NOT_FOUND)

        ResponseEntity.ok(address)
    }

    @GetMapping("/me")
    private suspend fun findMyself(@RequestHeader token: String): String = withContext(Dispatchers.IO) {
        log.info { "Obteniendo datos del usuario." }
        
        val user = getUserFromToken(token, userRepositoryCached, userMapper)
        if (user == null) json.encodeToString(ResponseEntity("User not found", HttpStatus.NOT_FOUND))
        else json.encodeToString(ResponseEntity(user, HttpStatus.OK))
    }
}