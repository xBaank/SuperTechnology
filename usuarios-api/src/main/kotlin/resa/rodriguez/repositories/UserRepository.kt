package resa.rodriguez.repositories

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import resa.rodriguez.models.User
import java.util.*

@Repository
interface UserRepository : CoroutineCrudRepository<User, UUID> {
    fun findFirstByEmailContaining(email: String) : Flow<User>
    fun findFirstByUsernameContaining(username: String) : Flow<User>
    fun findFirstByPhone(phone: String) : Flow<User>
    fun findAllByActivoOrderByCreatedAt(activo: Boolean) : Flow<User>
}