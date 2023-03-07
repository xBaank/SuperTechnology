package resa.rodriguez.dto

import kotlinx.serialization.Serializable
import resa.rodriguez.models.User
import resa.rodriguez.services.LocalDateSerializer
import java.time.LocalDate

/**
 * DTO used for login.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @property username
 * @property password
 */
@Serializable
data class UserDTOlogin(
    val username: String,
    val password: String
)

/**
 * DTO used for register.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
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
 * DTO used for creation.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
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
    val role: User.UserRole,
    val addresses: Set<String>,
    val avatar: String = "",
    val active: Boolean = true
)

/**
 * DTO used for the responses.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
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
    val role: User.UserRole,
    val addresses: Set<String>,
    val avatar: String,
    @Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDate,
    val active: Boolean
)

/**
 * DTO used for updating.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @property password
 * @property addresses
 */
@Serializable
data class UserDTOUpdated(
    val password: String,
    val addresses: Set<String>
)

/**
 * DTO used for updating a role.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @property email
 * @property role
 */
@Serializable
data class UserDTORoleUpdated(
    val email: String,
    val role: User.UserRole
)

/**
 * DTO used for the login/register/create responses.
 * It not only has the user's data, but also their token.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @property user
 * @property token
 */
@Serializable
data class UserDTOwithToken(
    val user: UserDTOresponse,
    val token: String
)