package resa.rodriguez.repositories

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import resa.rodriguez.models.User
import java.util.*

interface UserRepository : CoroutineCrudRepository<UUID, User> {
}