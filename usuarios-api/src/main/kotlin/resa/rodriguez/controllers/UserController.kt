package resa.rodriguez.controllers

import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import resa.rodriguez.config.APIConfig
import resa.rodriguez.dto.UserDTOlogin
import resa.rodriguez.dto.UserDTOregister
import resa.rodriguez.dto.UserDTOresponse
import resa.rodriguez.mappers.UserMapper
import resa.rodriguez.mappers.fromDTOtoAddresses
import resa.rodriguez.mappers.fromDTOtoUser
import resa.rodriguez.models.Address
import resa.rodriguez.models.UserRole
import resa.rodriguez.repositories.address.AddressRepositoryCached
import resa.rodriguez.repositories.user.UserRepositoryCached
import resa.rodriguez.services.checkToken
import resa.rodriguez.services.create
import resa.rodriguez.services.matches

private val log = KotlinLogging.logger {}

/**
 * Controlador rest para realizar operaciones CRUD con los usuarios
 *
 */
@RestController
@RequestMapping(APIConfig.API_PATH + "/users")
class UserController
@Autowired constructor(
    private val userMapper: UserMapper,
    private val userRepositoryCached: UserRepositoryCached,
    private val addressRepositoryCached: AddressRepositoryCached
) {
    @GetMapping("/list")
    suspend fun listUsers(token: String): ResponseEntity<out Any> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo listado de usuarios" }
        val checked = checkToken(token, UserRole.ADMIN)
        if (checked != null) return@withContext checked

        val res = userRepositoryCached.findAll().toList()

        ResponseEntity.ok(userMapper.toDTO(res))
    }

    @PostMapping("/register")
    suspend fun register(@Valid @RequestBody userDto: UserDTOregister): ResponseEntity<out Any> = withContext(Dispatchers.IO) {
        log.info { "Registro de usuario: ${userDto.username}" }

        try {
            val user = userDto.fromDTOtoUser() ?:
                return@withContext ResponseEntity("Password and repeated password does not match.", HttpStatus.BAD_REQUEST)

            val userSaved = userRepositoryCached.save(user)

            val addresses = userDto.fromDTOtoAddresses(userSaved.id!!)
            addresses.forEach { addressRepositoryCached.save(it) }

            ResponseEntity.ok(create(userSaved))
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    @GetMapping("/login")
    suspend fun login(@Valid @RequestBody userDto: UserDTOlogin): ResponseEntity<out Any> = withContext(Dispatchers.IO) {
        log.info { "Registro de usuario: ${userDto.email}" }

        try {
            val user = userRepositoryCached.findByEmail(userDto.email) ?:
                return@withContext ResponseEntity("Incorrect fields.", HttpStatus.NOT_FOUND)

            if (!matches(userDto.password, user.password.encodeToByteArray()))
                return@withContext ResponseEntity("Incorrect fields.", HttpStatus.NOT_FOUND)
            // Tecnicamente seria una bad request porque las contraseñas no coinciden, pero si lo
            // hacemos asi, el usuario sabria que el email existe y lo que falla es la contraseña,
            // por lo que podria iniciar un ataque de fuerza bruta para suplantar a otro usuario.

            ResponseEntity.ok(create(user))
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    @GetMapping("/list/address")
    suspend fun listAddresses(token: String): ResponseEntity<out Any> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo listado de direcciones" }
        val checked = checkToken(token, UserRole.ADMIN)
        if (checked != null) return@withContext checked

        ResponseEntity.ok(addressRepositoryCached.findAll().toList())
    }
}