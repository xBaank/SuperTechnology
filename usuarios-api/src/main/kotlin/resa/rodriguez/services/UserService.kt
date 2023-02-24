package resa.rodriguez.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import resa.rodriguez.dto.UserDTOcreate
import resa.rodriguez.dto.UserDTOregister
import resa.rodriguez.exceptions.UserExceptionBadRequest
import resa.rodriguez.exceptions.UserExceptionNotFound
import resa.rodriguez.mappers.fromDTOtoAddresses
import resa.rodriguez.mappers.fromDTOtoUser
import resa.rodriguez.models.User
import resa.rodriguez.repositories.address.AddressRepositoryCached
import resa.rodriguez.repositories.user.UserRepositoryCached

private val log = KotlinLogging.logger {}

@Service
class UserService
@Autowired constructor(
    private val userRepositoryCached: UserRepositoryCached,
    private val addressRepositoryCached: AddressRepositoryCached,
    private val passwordEncoder: PasswordEncoder
) : UserDetailsService {

    // Spring Security, no se puede suspender
    override fun loadUserByUsername(username: String): UserDetails = runBlocking {
        userRepositoryCached.findByUsername(username)
            ?: throw UserExceptionNotFound("Usuario con username $username no encontrado.")
    }

    // -- USERS --

    // Register, Create & Login Methods
    suspend fun register(userDto: UserDTOregister): User =
        withContext(Dispatchers.IO) {
            log.info { "Registro de usuario: ${userDto.username}" }

            try {
                val user = userDto.fromDTOtoUser()
                    ?: throw UserExceptionBadRequest("Password and repeated password does not match.")

                val userNew = user.copy(
                    password = passwordEncoder.encode(user.password)
                )

                val userSaved = userRepositoryCached.save(userNew)

                val addresses = userDto.fromDTOtoAddresses(userSaved.id!!)
                addresses.forEach { addressRepositoryCached.save(it) }

                return@withContext userSaved
            } catch (e: Exception) {
                throw UserExceptionBadRequest(e.message)
            }
        }

    suspend fun create(
        userDTOcreate: UserDTOcreate,
    ): User =
        withContext(Dispatchers.IO) {
            log.info { "Creando usuario por parte de un administrador" }

            val user = userDTOcreate.fromDTOtoUser()

            val userNew = user.copy(
                password = passwordEncoder.encode(user.password)
            )

            val userSaved = userRepositoryCached.save(userNew)

            val addresses = userDTOcreate.fromDTOtoAddresses(userSaved.id!!)
            addresses.forEach { addressRepositoryCached.save(it) }

            return@withContext userSaved
        }

    // "Find All" Methods
    suspend fun listUsers(user: User): List<User> =
        withContext(
            Dispatchers.IO
        ) {
            log.info { "Obteniendo listado de usuarios" }

            return@withContext userRepositoryCached.findAll().toList<User>()
        }
}