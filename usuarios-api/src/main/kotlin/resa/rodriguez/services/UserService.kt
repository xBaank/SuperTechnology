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

/**
 * Service that will execute the corresponding queries using the repositories and then return the correct DTOs.
 * @property userRepositoryCached User repository cached.
 * @property addressRepositoryCached Address repository cached.
 * @property passwordEncoder Password encoder.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
@Service
class UserService
@Autowired constructor(
    private val userRepositoryCached: UserRepositoryCached,
    private val addressRepositoryCached: AddressRepositoryCached,
    private val passwordEncoder: PasswordEncoder
) : UserDetailsService {
    /**
     * Function that will return a UserDetails object with the given username. Cannot be a suspended function.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param username Username to be searched.
     * @return User whose username matches the given parameter.
     * @throws UserExceptionNotFound when it cannot find that user.
     */
    override fun loadUserByUsername(username: String): UserDetails = runBlocking {
        userRepositoryCached.findByUsername(username)
            ?: throw UserExceptionNotFound("Usuario con username $username no encontrado.")
    }

    /**
     * Function for registering.
     * Saves the user in the database along with their encrypted password, thanks to Bcrypt.
     * The registered user will always have the USER role.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param userDto DTO with the information we need for registering.
     * @return The inserted user.
     * @throws UserExceptionBadRequest when it cannot insert it or passwords do not match.
     */
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

    /**
     * Function for inserting a new user by an administrator.
     * Its purpose is the same as the Register function,
     * but with the difference that this one can create users with any role.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param userDTOcreate DTO with the information we need to insert.
     * @return The inserted user.
     */
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

    /**
     * Function for getting a list with every existing user.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @return A list containing every user from the database.
     */
    suspend fun listUsers(): List<User> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo listado de usuarios" }

            userRepositoryCached.findAll().toList()
        }

    /**
     * Function for getting a particular user from a given username.
     * Different from loadUserByUsername in that this one is indeed a suspended function,
     * therefore this one will be used whenever we want to find a user.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param username Username to be searched.
     * @return User whose username matches the given parameter.
     * @throws UserExceptionNotFound when a user is not found.
     */
    suspend fun findByUsername(username: String): User = withContext(Dispatchers.IO) {
        log.info { "Obteniendo usuario con username: $username " }

        userRepositoryCached.findByUsername(username)
            ?: throw UserExceptionNotFound("User with username $username not found.")
    }

    /**
     * Function for getting a particular user from a given UUID.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param id UUID to be searched.
     * @return User whose id matches the given parameter.
     * @throws UserExceptionNotFound when a user is not found.
     */
    suspend fun findById(id: UUID): User = withContext(Dispatchers.IO) {
        log.info { "Obteniendo usuario con id: $id" }

        userRepositoryCached.findById(id) ?: throw UserExceptionNotFound("User with id $id not found.")
    }

    /**
     * Function for getting a particular user from a given email.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param email Email to be searched.
     * @return User whose email matches the given parameter.
     * @throws UserExceptionNotFound when a user is not found.
     */
    suspend fun findByEmail(email: String): User = withContext(Dispatchers.IO) {
        log.info { "Obteniendo usuario con email: $email" }

        userRepositoryCached.findByEmail(email) ?: throw UserExceptionNotFound("User with email $email not found.")
    }

    /**
     * Function for getting a particular user from a given phone number.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param phone Phone number to be searched.
     * @return User whose phone number matches the given parameter.
     * @throws UserExceptionNotFound when a user is not found.
     */
    suspend fun findByUserPhone(phone: String): User = withContext(Dispatchers.IO) {
        log.info { "Obteniendo usuario con phone: $phone" }

        userRepositoryCached.findByPhone(phone) ?: throw UserExceptionNotFound("User with phone $phone not found.")
    }

    /**
     * Function for getting every address from a user whose ID matches the given parameter.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param userId UUID from the user whose addresses we want to find.
     * @return Flow of every address from that user.
     */
    suspend fun findAllFromUserId(userId: UUID): Flow<Address> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo direcciones del usuario: $userId " }

        addressRepositoryCached.findAllFromUserId(userId)
    }

    /**
     * Function for getting every user in a paginated manner, and sorted by the given parameter.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param page Page number to be searched.
     * @param size Size of the page.
     * @param sortBy How we want to sort the contents of the page.
     * @return Page with the corresponding user DTOs.
     * @throws UserExceptionNotFound when a page is not found.
     */
    suspend fun findAllPaging(page: Int, size: Int, sortBy: String): Page<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuarios de la pagina: $page " }

            val pageRequest = PageRequest.of(page, size, Sort.Direction.ASC, sortBy)
            userRepositoryCached.findAllPaged(pageRequest).firstOrNull()
                ?: throw UserExceptionNotFound("Page $page not found.")
        }

    /**
     * Function for getting every user whose activity matches the given parameter.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param active Boolean that determines the type of activity that we want to filter.
     * @return List of every user with the specified activity.
     */
    suspend fun findAllByActive(active: Boolean): List<User> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo todos los usuarios con actividad = $active" }

        userRepositoryCached.findByActivo(active).toList()
    }

    /**
     * Function for updating your own password and/or list of addresses.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param user User to be updated.
     * @param userDTOUpdated DTO containing the new information.
     * @return User with the updated fields.
     */
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

    /**
     * Function for updating your own avatar.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param user User to be updated.
     * @return User with the updated fields.
     */
    suspend fun updateAvatar(user: User): User = withContext(Dispatchers.IO) {
        userRepositoryCached.save(user)
    }

    /**
     * Function for switching a user's activity from a given email.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param email Email of the user whose activity is going to be switched.
     * @return User with the updated activity.
     * @throws UserExceptionNotFound when a user is not found.
     */
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

    /**
     * Function for updating a user's role from a given email.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param userDTORoleUpdated DTO containing the user's email and new role.
     * @return User with the updated role.
     * @throws UserExceptionNotFound when a user is not found.
     */
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

    /**
     * Function for deleting a user from a given email.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param email Email of the user to be deleted.
     * @return The deleted user.
     * @throws UserExceptionNotFound when a user is not found.
     */
    suspend fun delete(email: String): User = withContext(Dispatchers.IO) {
        val user = userRepositoryCached.findByEmail(email)
            ?: throw UserExceptionNotFound("User with email: $email not found.")

        addressRepositoryCached.deleteAllByUserId(user.id!!)

        userRepositoryCached.deleteById(user.id)
            ?: throw UserExceptionNotFound("User with email: $email not found.")
    }

    /**
     * Function for getting every address.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @return A list with every address present in the database.
     */
    suspend fun findAllAddresses(): List<Address> = withContext(Dispatchers.IO) {
        addressRepositoryCached.findAll().toList()
    }

    /**
     * Function for getting a paginated list of addresses.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param page Number of the page to be searched.
     * @param size Size of the page to be searched.
     * @param sortBy How we want to sort the contents of the page.
     * @return Page with the corresponding Addresses.
     * @throws AddressExceptionNotFound when a page is not found.
     */
    suspend fun findAllPagingAddresses(page: Int, size: Int, sortBy: String): Page<Address> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo direcciones de la pagina: $page " }

            val pageRequest = PageRequest.of(page, size, Sort.Direction.ASC, sortBy)
            addressRepositoryCached.findAllPaged(pageRequest).firstOrNull()
                ?: throw AddressExceptionNotFound("Page $page not found.")
        }

    /**
     * Function for getting all addresses from a given user.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param userId UUID from the user whose addresses we want to find.
     * @return String with the corresponding addresses, separated by commas.
     * @throws AddressExceptionNotFound when no address has been found.
     */
    suspend fun listAddressesByUserId(userId: UUID): String = withContext(Dispatchers.IO) {
        val address = addressRepositoryCached.findAllFromUserId(userId).toList()

        if (address.isEmpty()) throw AddressExceptionNotFound("Addresses with userId: $userId not found.")
        else {
            var add = ""
            address.forEach { add += "${it.address}," }
            add.dropLast(1) // this way we delete the last comma.
        }
    }

    /**
     * Function for getting an address from a given id.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param id UUID from the address we want to find.
     * @return String with the corresponding address name.
     * @throws AddressExceptionNotFound when no address has been found.
     */
    suspend fun findAddressById(id: UUID): String = withContext(Dispatchers.IO) {
        val addr = addressRepositoryCached.findById(id)
            ?: throw AddressExceptionNotFound("Address with id: $id not found.")
        addr.address
    }

    /**
     * Function for getting an address from a given name.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param name Name of the address we want to find.
     * @return String with the corresponding address name.
     * @throws AddressExceptionNotFound when no address has been found.
     */
    suspend fun findAddressByName(name: String): String = withContext(Dispatchers.IO) {
        val addr = addressRepositoryCached.findAllByAddress(name).firstOrNull()
            ?: throw AddressExceptionNotFound("Address with name: $name not found.")
        addr.address
    }

    /**
     * Function for deleting an address with a given name from a user with the specified email.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param name Name of the address we want to find.
     * @param email Email of the user whose addresses we want to check.
     * @return String with the deleted message.
     * @throws AddressExceptionNotFound when no address has been found.
     * @throws UserExceptionNotFound when no user has been found.
     * @throws AddressExceptionBadRequest when the address has not been successfully deleted.
     */
    suspend fun deleteAddress(name: String, email: String): String = withContext(Dispatchers.IO) {
        val address = addressRepositoryCached.findAllByAddress(name).firstOrNull()
        val u = userRepositoryCached.findByEmail(email)

        if (address == null) throw AddressExceptionNotFound("Address not found.")
        if (u == null) throw UserExceptionNotFound("User not found.")

        val addresses = addressRepositoryCached.findAllFromUserId(u.id!!).toSet()

        if (address.userId == u.id && addresses.size > 1) {
            val addr = addressRepositoryCached.deleteById(address.id!!)
            "Direccion ${addr?.address} eliminada."
        } else throw AddressExceptionBadRequest("No ha sido posible eliminar la direccion.")
    }
}