package resa.rodriguez.repositories.user

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import resa.rodriguez.models.User
import java.util.*

@Repository
interface UserRepository : CoroutineCrudRepository<User, UUID> {
    fun findFirstByEmailContaining(email: String) : Flow<User>
    fun findByUsernameContaining(username: String) : Flow<User>
    fun findFirstByPhone(phone: String) : Flow<User>
    fun findAllByActiveOrderByCreatedAt(active: Boolean) : Flow<User>
    fun findAllBy(page: Pageable?) : Flow<User>
}