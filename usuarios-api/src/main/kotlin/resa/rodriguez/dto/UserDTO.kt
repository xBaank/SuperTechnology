package resa.rodriguez.dto

import resa.rodriguez.models.UserRole
import java.time.LocalDate
import java.util.*

data class UserDTOlogin (
    val email: String,
    val password: String
)

data class UserDTOregister (
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val email: String,
    val password: String,
    val repeatPassword: String,
    val phone: String,
    val addresses: Set<String>
)

data class UserDTOcreate (
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val email: String,
    val password: String,
    val phone: String,
    val role: UserRole,
    val addresses: Set<String>,
    val avatar: String = "",
    val active: Boolean = true
)

data class UserDTOresponse (
    val username: String,
    val email: String,
    val role: UserRole,
    val addresses: Set<String>,
    val avatar: String,
    val createdAt: LocalDate,
    val active: Boolean
)