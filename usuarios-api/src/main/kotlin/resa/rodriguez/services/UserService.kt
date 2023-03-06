package resa.rodriguez.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import resa.rodriguez.dto.*
import resa.rodriguez.exceptions.AddressExceptionBadRequest
import resa.rodriguez.exceptions.AddressExceptionNotFound
import resa.rodriguez.exceptions.UserExceptionBadRequest
import resa.rodriguez.exceptions.UserExceptionNotFound
import resa.rodriguez.mappers.fromDTOtoAddresses
import resa.rodriguez.mappers.fromDTOtoUser
import resa.rodriguez.mappers.toAddress
import resa.rodriguez.models.Address
import resa.rodriguez.models.User
import resa.rodriguez.repositories.address.AddressRepositoryCached
import resa.rodriguez.repositories.user.UserRepositoryCached
import java.util.*

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

    // Register & Create Methods
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

                userSaved
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

            userSaved
        }

    // "Find All" Methods
    suspend fun listUsers(): List<User> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo listado de usuarios" }

            userRepositoryCached.findAll().toList()
        }

    suspend fun findByUsername(username: String): User = withContext(Dispatchers.IO) {
        log.info { "Obteniendo usuario con username: $username " }

        userRepositoryCached.findByUsername(username)
            ?: throw UserExceptionNotFound("User with username $username not found.")
    }

    suspend fun findById(id: UUID): User = withContext(Dispatchers.IO) {
        log.info { "Obteniendo usuario con id: $id" }

        userRepositoryCached.findById(id) ?: throw UserExceptionNotFound("User with id $id not found.")
    }

    suspend fun findByEmail(email: String): User = withContext(Dispatchers.IO) {
        log.info { "Obteniendo usuario con email: $email" }

        userRepositoryCached.findByEmail(email) ?: throw UserExceptionNotFound("User with email $email not found.")
    }

    suspend fun findByUserPhone(phone: String): User = withContext(Dispatchers.IO) {
        log.info { "Obteniendo usuario con phone: $phone" }

        userRepositoryCached.findByPhone(phone) ?: throw UserExceptionNotFound("User with phone $phone not found.")
    }

    // ADDRESSES

    // "FindAll" Methods
    suspend fun findAllFromUserId(userId: UUID): Flow<Address> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo direcciones del usuario: $userId " }

        addressRepositoryCached.findAllFromUserId(userId)
    }

    suspend fun findAllPaging(page: Int, size: Int, sortBy: String): Page<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuarios de la pagina: $page " }

            val pageRequest = PageRequest.of(page, size, Sort.Direction.ASC, sortBy)
            userRepositoryCached.findAllPaged(pageRequest).firstOrNull()
                ?: throw UserExceptionNotFound("Page $page not found.")
        }

    suspend fun findAllByActive(active: Boolean): List<User> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo todos los usuarios con actividad = $active" }

        userRepositoryCached.findByActivo(active).toList()
    }

    suspend fun updateMySelf(user: User, userDTOUpdated: UserDTOUpdated): User = withContext(Dispatchers.IO) {
        val updatedPassword = if (userDTOUpdated.password.isBlank()) user.password
        else passwordEncoder.encode(userDTOUpdated.password)

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

        userRepositoryCached.save(userUpdated)
    }

    suspend fun updateAvatar(user: User): User = withContext(Dispatchers.IO) {
        userRepositoryCached.save(user)
    }

    suspend fun switchActivity(email: String): User = withContext(Dispatchers.IO) {
        val user = userRepositoryCached.findByEmail(email)
            ?: throw UserExceptionNotFound("User with email: $email not found.")

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

        userRepositoryCached.save(userUpdateActivity)
    }

    suspend fun updateRoleByEmail(userDTORoleUpdated: UserDTORoleUpdated): User = withContext(Dispatchers.IO) {
        val user = userRepositoryCached.findByEmail(userDTORoleUpdated.email)
            ?: throw UserExceptionNotFound("User with email: ${userDTORoleUpdated.email} not found.")

        val updatedRole =
            if (userDTORoleUpdated.role.name.uppercase() != (User.UserRole.USER.name) &&
                userDTORoleUpdated.role.name.uppercase() != (User.UserRole.ADMIN.name) &&
                userDTORoleUpdated.role.name.uppercase() != (User.UserRole.SUPER_ADMIN.name)
            ) {
                user.role
            } else userDTORoleUpdated.role

        val userUpdated = user.copy(
            role = updatedRole
        )
        userRepositoryCached.save(userUpdated)
    }

    suspend fun delete(email: String): User = withContext(Dispatchers.IO) {
        val user = userRepositoryCached.findByEmail(email)
            ?: throw UserExceptionNotFound("User with email: $email not found.")

        addressRepositoryCached.deleteAllByUserId(user.id!!)

        userRepositoryCached.deleteById(user.id)
            ?: throw UserExceptionNotFound("User with email: $email not found.")
    }

    suspend fun findAllAddresses(): List<Address> = withContext(Dispatchers.IO) {
        addressRepositoryCached.findAll().toList()
    }

    suspend fun findAllPagingAddresses(page: Int, size: Int, sortBy: String ): Page<Address> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo direcciones de la pagina: $page " }

            val pageRequest = PageRequest.of(page, size, Sort.Direction.ASC, sortBy)
            addressRepositoryCached.findAllPaged(pageRequest).firstOrNull()
                ?: throw AddressExceptionNotFound("Page $page not found.")
        }

    suspend fun listAddressesByUserId(userId: UUID): String = withContext(Dispatchers.IO) {
        val address = addressRepositoryCached.findAllFromUserId(userId).toList()

        if (address.isEmpty()) throw AddressExceptionNotFound("Addresses with userId: $userId not found.")
        else {
            var add = ""
            address.forEach { add += "${it.address}," }
            add.dropLast(1) // asi quitamos la ultima coma
        }
    }

    suspend fun findAddressById(id: UUID): String = withContext(Dispatchers.IO) {
        val addr = addressRepositoryCached.findById(id)
            ?: throw AddressExceptionNotFound("Address with id: $id not found.")
        addr.address
    }

    suspend fun findAddressByName(name: String): String = withContext(Dispatchers.IO) {
        val addr = addressRepositoryCached.findAllByAddress(name).firstOrNull()
            ?: throw AddressExceptionNotFound("Address with name: $name not found.")
        addr.address
    }

    suspend fun deleteAddress(name: String, email: String): String = withContext(Dispatchers.IO) {
        val address = addressRepositoryCached.findAllByAddress(name).firstOrNull()
        val u = userRepositoryCached.findByEmail(email)

        if (address == null) throw AddressExceptionNotFound("Address not found.")
        if (u == null) throw UserExceptionNotFound("User not found.")

        val addresses = addressRepositoryCached.findAllFromUserId(u.id!!).toSet()

        if (address.userId == u.id && addresses.isNotEmpty()) {
            val addr = addressRepositoryCached.deleteById(address.id!!)
            "Direccion ${addr?.address} eliminada."
        } else throw AddressExceptionBadRequest("No ha sido posible eliminar la direccion.")
    }
}