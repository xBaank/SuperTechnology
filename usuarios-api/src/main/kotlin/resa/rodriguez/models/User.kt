package resa.rodriguez.models

import java.util.UUID

data class User (
    val id: UUID,
    val username: String,
    val email: String,
    val password: String,
    val phone: String,
    val role: UserRole,
    val createdAt: String,
    val avatar: String,
    val activo: Boolean
)

data class Address (
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val address: String
)

enum class UserRole {
    USER,ADMIN,SUPE_RADMIN
}