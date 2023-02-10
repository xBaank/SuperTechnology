package resa.rodriguez.repositories.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import resa.rodriguez.models.User
import java.util.*

/**
 * Repositorio cacheado de usuarios
 *
 * @property repo
 */
@Repository
class UserRepositoryCached
@Autowired constructor(
    private val repo: UserRepository
) : IUserRepositoryCached {
    override suspend fun findAll(): Flow<User> = withContext(Dispatchers.IO) {
        repo.findAll()
    }

    override suspend fun findAllPaged(page: PageRequest): Flow<Page<User>> {
        return repo.findAllBy(page).toList()
            .windowed(page.pageSize, page.pageSize, true)
            .map { PageImpl(it, page, repo.count()) }
            .asFlow()
    }

    override suspend fun findByActivo(activo: Boolean): Flow<User> = withContext(Dispatchers.IO) {
        repo.findAllByActiveOrderByCreatedAt(activo)
    }

    override suspend fun findById(id: UUID): User? = withContext(Dispatchers.IO) {
        repo.findById(id)
    }

    override suspend fun findByEmail(email: String): User? = withContext(Dispatchers.IO) {
        repo.findFirstByEmailContaining(email).firstOrNull()
    }

    override suspend fun findByUsername(username: String): Flow<User> {
        TODO("Not yet implemented")
    }

    override suspend fun findByPhone(phone: String): User? {
        TODO("Not yet implemented")
    }

    override suspend fun save(user: User): User {
        TODO("Not yet implemented")
    }

    override suspend fun deleteById(id: UUID): User? {
        TODO("Not yet implemented")
    }

    override suspend fun setInactive(id: UUID): User? {
        TODO("Not yet implemented")
    }

    override suspend fun update(id: UUID, user: User): User? {
        TODO("Not yet implemented")
    }

    override suspend fun updateCapado(id: UUID, user: User): User? {
        TODO("Not yet implemented")
    }
}