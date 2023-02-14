package resa.rodriguez.dto

import kotlinx.serialization.Serializable
import resa.rodriguez.models.UserRole
import resa.rodriguez.services.LocalDateSerializer
import java.time.LocalDate

/**
 * Dto usada para el login de usuarios
 *
 * @property email
 * @property password
 */
@Serializable
data class UserDTOlogin(
    val email: String,
    val password: String
)

/**
 * Dto usada para el registro de un usuario personalmente
 *
 * @property username
 * @property email
 * @property password
 * @property repeatPassword
 * @property phone
 * @property addresses
 */
@Serializable
data class UserDTOregister(
    val username: String,
    val email: String,
    val password: String,
    val repeatPassword: String,
    val phone: String,
    val addresses: Set<String>
)

/**
 * Dto usada para el registro de un usuario por parte de un SUPER_ADMIN
 *
 * @property username
 * @property email
 * @property password
 * @property phone
 * @property role
 * @property addresses
 * @property avatar
 * @property active
 */
@Serializable
data class UserDTOcreate(
    val username: String,
    val email: String,
    val password: String,
    val phone: String,
    val role: UserRole,
    val addresses: Set<String>,
    val avatar: String = "",
    val active: Boolean = true
)

/**
 * Dto usada para mostrar al usuario al cliente, evitando los datos mas sensibles.
 *
 * @property username
 * @property email
 * @property role
 * @property addresses
 * @property avatar
 * @property createdAt
 * @property active
 */
@Serializable
data class UserDTOresponse(
    val username: String,
    val email: String,
    val role: UserRole,
    val addresses: Set<String>,
    val avatar: String,
    @Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDate,
    val active: Boolean
)