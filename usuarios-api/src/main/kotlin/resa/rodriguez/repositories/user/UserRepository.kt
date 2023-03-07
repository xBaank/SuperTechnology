package resa.rodriguez.repositories.user

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import resa.rodriguez.models.User
import java.util.*

/**
 * Repository that will execute the different CRUD operations needed to work with the database.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
@Repository
interface UserRepository : CoroutineCrudRepository<User, UUID> {
    fun findFirstByEmail(email: String): Flow<User>
    fun findByUsername(username: String): Flow<User>
    fun findFirstByPhone(phone: String): Flow<User>
    fun findAllByActiveOrderByCreatedAt(active: Boolean): Flow<User>
    fun findAllBy(page: Pageable?): Flow<User>
}