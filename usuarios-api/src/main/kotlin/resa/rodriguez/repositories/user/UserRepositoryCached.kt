package resa.rodriguez.repositories.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import resa.rodriguez.dto.UserDTOresponse
import resa.rodriguez.exceptions.UserExceptionNotFound
import resa.rodriguez.mappers.toDTO
import resa.rodriguez.models.User
import resa.rodriguez.repositories.address.AddressRepository
import java.util.*

/**
 * Repository that will execute the different CRUD operations needed to work with the database.
 * It has a cache.
 * @property repo User repository.
 * @property aRepo Address repository.
 * @author Mario Gonzalez, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
@Repository
class UserRepositoryCached
@Autowired constructor(
    private val repo: UserRepository,
    private val aRepo: AddressRepository,
) : IUserRepositoryCached {
    /**
     * Function that will return a flow with every user present in the database.
     * @author Mario Gonzalez, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @return Flow with every user in the database.
     */
    override suspend fun findAll(): Flow<User> = withContext(Dispatchers.IO) {
        repo.findAll()
    }

    /**
     * Function that will return a flow of pages of user DTOs.
     * @author Mario Gonzalez, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param page Page request.
     * @return A flow of pages containing user DTOs.
     */
    override suspend fun findAllPaged(page: PageRequest): Flow<Page<UserDTOresponse>> {
        return repo.findAllBy(page).toList().map {
            val addresses = it.id?.let { id -> aRepo.findAllByUserId(id).toSet() } ?: setOf()
            it.toDTO(addresses)
        }
            .windowed(page.pageSize, page.pageSize, true)
            .map { PageImpl(it, page, repo.count()) }
            .asFlow()
    }

    /**
     * Function that will return a flow of users filtered by the given activity and filtered by creation date.
     * @author Mario Gonzalez, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param activo Type of activity to search for.
     * @return Flow of users filtered by whether they are active or not.
     */
    override suspend fun findByActivo(activo: Boolean): Flow<User> = withContext(Dispatchers.IO) {
        repo.findAllByActiveOrderByCreatedAt(activo)
    }

    /**
     * Function that will return a user whose id matches the specified id.
     * @author Mario Gonzalez, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param id UUID to search for.
     * @return A user whose id matches the one given in the id parameter, or null if it is not found.
     */
    @Cacheable("usuarios")
    override suspend fun findById(id: UUID): User? = withContext(Dispatchers.IO) {
        repo.findById(id)
    }

    /**
     * Function that will return a user whose email matches the specified email.
     * @author Mario Gonzalez, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param email Email to search for.
     * @return A user whose email matches the one given in the email parameter, or null if it is not found.
     */
    @Cacheable("usuarios")
    override suspend fun findByEmail(email: String): User? = withContext(Dispatchers.IO) {
        repo.findFirstByEmail(email).firstOrNull()
    }

    /**
     * Function that will return a user whose username matches the specified username.
     * @author Mario Gonzalez, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param username Username to search for.
     * @return A user whose username matches the one given in the username parameter, or null if it is not found.
     */
    @Cacheable("usuarios")
    override suspend fun findByUsername(username: String): User? = withContext(Dispatchers.IO) {
        repo.findByUsername(username).firstOrNull()
    }

    /**
     * Function that will return a user whose phone number matches the specified phone.
     * @author Mario Gonzalez, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param phone Phone number to search for.
     * @return A user whose phone number matches the one given in the phone parameter, or null if it is not found.
     */
    @Cacheable("usuarios")
    override suspend fun findByPhone(phone: String): User? = withContext(Dispatchers.IO) {
        repo.findFirstByPhone(phone).firstOrNull()
    }

    /**
     * Function that will return a user once it has been saved into the database.
     * @author Mario Gonzalez, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param user User to be saved.
     * @return The saved user.
     */
    @CachePut("usuarios")
    override suspend fun save(user: User): User = withContext(Dispatchers.IO) {
        repo.save(user)
    }

    /**
     * Function that will return the deleted user once it has been deleted from the database,
     * or null if a user with the specified id is not found.
     * @author Mario Gonzalez, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param id UUID to be searched for.
     * @return The deleted user, or null if a user with the given id was not found.
     */
    @CacheEvict("usuarios")
    override suspend fun deleteById(id: UUID): User? = withContext(Dispatchers.IO) {
        val user = repo.findById(id) ?: return@withContext null
        user.id?.let { repo.deleteById(it) }
        user
    }
}