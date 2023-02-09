package resa.rodriguez.repositories.user

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import resa.rodriguez.models.User
import java.util.UUID

interface IUserRepositoryCached {
    suspend fun findAll() : Flow<User>
    suspend fun findAllPaged(page: PageRequest) : Flow<Page<User>>
    suspend fun findByActivo(activo: Boolean) : Flow<User>
    suspend fun findById(id: UUID) : User?
    suspend fun findByEmail(email: String) : User?
    suspend fun findByUsername(username: String) : Flow<User>
    suspend fun findByPhone(phone: String) : User?
    suspend fun save(user: User) : User
    suspend fun deleteById(id: UUID) : User?
    suspend fun setInactive(id: UUID) : User?
    suspend fun update(id: UUID, user: User) : User?
    suspend fun updateCapado(id: UUID, user: User) : User?
}