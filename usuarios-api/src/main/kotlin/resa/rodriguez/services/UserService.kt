package resa.rodriguez.services

import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
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
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import resa.rodriguez.config.APIConfig
import resa.rodriguez.config.security.jwt.JwtTokensUtils
import resa.rodriguez.controllers.StorageController
import resa.rodriguez.dto.*
import resa.rodriguez.exceptions.AddressExceptionNotFound
import resa.rodriguez.exceptions.UserExceptionBadRequest
import resa.rodriguez.exceptions.UserExceptionNotFound
import resa.rodriguez.mappers.UserMapper
import resa.rodriguez.mappers.fromDTOtoAddresses
import resa.rodriguez.mappers.fromDTOtoUser
import resa.rodriguez.mappers.toAddress
import resa.rodriguez.models.Address
import resa.rodriguez.models.User
import resa.rodriguez.models.UserRole
import resa.rodriguez.repositories.address.AddressRepositoryCached
import resa.rodriguez.repositories.user.UserRepositoryCached
import java.util.*

private val log = KotlinLogging.logger {}


@Service
class UserService
@Autowired constructor(
    private val userMapper: UserMapper,
    private val userRepositoryCached: UserRepositoryCached,
    private val addressRepositoryCached: AddressRepositoryCached,
    private val jwtTokenUtils: JwtTokensUtils,
    private val storageController: StorageController,
    private val passwordEncoder: PasswordEncoder,
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

                val userSaved = userRepositoryCached.save(user)

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

            val userSaved = userRepositoryCached.save(user)

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

    private suspend fun getAllPaging(
        @RequestHeader token: String,
        @RequestParam(defaultValue = APIConfig.PAGINATION_INIT) page: Int = 0,
        @RequestParam(defaultValue = APIConfig.PAGINATION_SIZE) size: Int = 10,
        @RequestParam(defaultValue = APIConfig.PAGINATION_SORT) sortBy: String = "created_at",
    ): ResponseEntity<Page<UserDTOresponse>> = withContext(Dispatchers.IO) {
        log.info { "Buscando usuarios paginados || Pagina: $page" }

        val pageRequest = PageRequest.of(page, size, Sort.Direction.ASC, sortBy)
        val pageResponse = userRepositoryCached.findAllPaged(pageRequest).firstOrNull()

        if (pageResponse != null) {
            ResponseEntity.ok(pageResponse)
        } else throw UserExceptionNotFound("Page not found.")
    }

    private suspend fun listUsersActive(
        @PathVariable active: Boolean,
        @RequestHeader token: String
    ): ResponseEntity<List<UserDTOresponse>> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo listado de usuarios activados" }

        val res = userRepositoryCached.findByActivo(active).toList()

        ResponseEntity.ok(userMapper.toDTO(res))
    }

    // "Find One" Methods
    private suspend fun findByUsername(
        @PathVariable username: String,
        @RequestHeader token: String
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con username: $username" }

            val user = userRepositoryCached.findByUsername(username)
                ?: throw UserExceptionNotFound("User with name: $username not found.")

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

    private suspend fun findByUserId(
        @PathVariable userId: UUID,
        @RequestHeader token: String
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con id: $userId" }

            val user = userRepositoryCached.findById(userId)
                ?: throw UserExceptionNotFound("User with id: $userId not found")

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

    private suspend fun findByUserEmail(
        @PathVariable userEmail: String,
        @RequestHeader token: String
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con email: $userEmail" }

            val user = userRepositoryCached.findByEmail(userEmail)
                ?: throw UserExceptionNotFound("User with email: $userEmail not found")

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

    private suspend fun findByUserPhone(
        @PathVariable userPhone: String,
        @RequestHeader token: String
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo usuario con telefono: $userPhone" }

            val user = userRepositoryCached.findByPhone(userPhone)
                ?: throw UserExceptionNotFound("User with phone: $userPhone not found")

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

    // "Update" Methods

    private suspend fun updateMySelf(
        @RequestHeader token: String,
        @Valid @RequestBody userDTOUpdated: UserDTOUpdated
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        log.info { "Actualizando usuario" }

        val user = jwtTokenUtils.getUserFromToken(token, userRepositoryCached)
            ?: throw UserExceptionNotFound("User not found")

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

        ResponseEntity.ok(userMapper.toDTO(userSaved))
    }

    private suspend fun updateAvatar(
        @RequestHeader token: String,
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        val user = jwtTokenUtils.getUserFromToken(token, userRepositoryCached)
            ?: throw UserExceptionNotFound("User not found")

        val response = storageController.uploadFile(file)
        val avatarUrl = response.body?.get("url")
            ?: throw UserExceptionNotFound("Url not found.")

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

        ResponseEntity.ok(userMapper.toDTO(userSaved))
    }

    private suspend fun switchActivityByEmail(
        @PathVariable email: String,
        @RequestHeader token: String
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Cambio de actividad por email" }

            val user = userRepositoryCached.findByEmail(email)
                ?: throw UserExceptionNotFound("User with email: $email not found")

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

            ResponseEntity.ok(userMapper.toDTO(userSaved))
        }

    private suspend fun updateRoleByEmail(
        @Valid @RequestBody userDTORoleUpdated: UserDTORoleUpdated,
        @RequestHeader token: String
    ): ResponseEntity<UserDTOresponse> = withContext(Dispatchers.IO) {
        log.info { "Actualizando rol de usuario con email: ${userDTORoleUpdated.email}" }

        val user = userRepositoryCached.findByEmail(userDTORoleUpdated.email)
            ?: throw UserExceptionNotFound("User with email: ${userDTORoleUpdated.email} not found")

        val updatedRole =
            if (userDTORoleUpdated.role.name.uppercase() != (UserRole.USER.name) ||
                userDTORoleUpdated.role.name.uppercase() != (UserRole.ADMIN.name) ||
                userDTORoleUpdated.role.name.uppercase() != (UserRole.ADMIN.name)
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

        ResponseEntity.ok(userMapper.toDTO(userSaved))
    }

    // "Delete" Methods
    private suspend fun deleteUser(
        @PathVariable email: String,
        @RequestHeader token: String
    ): ResponseEntity<UserDTOresponse> =
        withContext(Dispatchers.IO) {
            log.info { "Eliminando al usuario de forma definitiva junto a sus direcciones asociadas" }

            val user = userRepositoryCached.findByEmail(email)
                ?: throw UserExceptionNotFound("User with email: $email not found.")

            addressRepositoryCached.deleteAllByUserId(user.id!!)

            val deleted = userRepositoryCached.deleteById(user.id)
                ?: throw UserExceptionNotFound("User with email: $email not found.")

            ResponseEntity.ok(userMapper.toDTO(deleted))
        }

    // "Me" Method
    private suspend fun findMySelf(@AuthenticationPrincipal user: User): ResponseEntity<UserDTOresponse> = withContext(
        Dispatchers.IO
    ) {
        log.info { "Obteniendo datos del usuario." }

        ResponseEntity.ok(userMapper.toDTO(user))
    }

    // -- ADDRESSES --

    // "Find All" Methods
    private suspend fun listAddresses(@RequestHeader token: String): ResponseEntity<List<Address>> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo listado de direcciones" }

            ResponseEntity.ok(addressRepositoryCached.findAll().toList())
        }

    private suspend fun listAddressesByUserId(
        @PathVariable userId: UUID,
        @RequestHeader token: String
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Obteniendo direcciones de usuario con id: $userId" }

        val address = addressRepositoryCached.findAllFromUserId(userId).toList()

        if (address.isEmpty()) throw AddressExceptionNotFound("Addresses with userId: $userId not found.")
        else {
            val add = ""
            address.forEach { add.plus("${it.address},") }
            add.dropLast(1) // asi quitamos la ultima coma
            ResponseEntity.ok(add)
        }
    }

    // "Find One" Methods
    private suspend fun findById(@PathVariable id: UUID, @RequestHeader token: String): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Obteniendo direccion con id: $id" }

            val address = addressRepositoryCached.findById(id)
                ?: throw AddressExceptionNotFound("Address with id: $id not found.")

            ResponseEntity.ok(address.address)
        }


    private suspend fun findByName(@PathVariable name: String, @RequestHeader token: String): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            log.info { "Buscando direccion con nombre: $name" }

            val address = addressRepositoryCached.findAllByAddress(name).firstOrNull()
                ?: throw AddressExceptionNotFound("Address with name: $name not found.")

            ResponseEntity.ok(address.address)
        }

    // "Delete" Methods

    @DeleteMapping("/address/{name}")
    private suspend fun deleteAddress(
        @PathVariable name: String,
        @RequestHeader token: String
    ): ResponseEntity<String> = withContext(Dispatchers.IO) {
        log.info { "Eliminando direccion: $name" }

        val userDto = jwtTokenUtils.getUserDTOFromToken(token, userRepositoryCached, userMapper)
            ?: throw UserExceptionNotFound("User not found.")

        val address = addressRepositoryCached.findAllByAddress(name).firstOrNull()
        val user = userRepositoryCached.findByEmail(userDto.email)

        if (address == null) throw AddressExceptionNotFound("Address not found.")
        if (user == null) throw UserExceptionNotFound("User not found.")

        val addresses = addressRepositoryCached.findAllFromUserId(user.id!!).toSet()

        if (address.userId == user.id && addresses.size > 1) {
            val addr = addressRepositoryCached.deleteById(address.id!!)
            ResponseEntity.ok("Direccion $addr eliminada.")
        } else throw UserExceptionBadRequest("No ha sido posible eliminar la direccion.")
    }
}