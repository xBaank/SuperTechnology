package resa.rodriguez.models

import jakarta.validation.constraints.NotEmpty
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDate
import java.util.UUID

/**
 * Model for users.
 * @author Mario Gonzalez, Daniel Rodriguez, Joan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @property id Main identifier, UUID.
 * @property username Username, String.
 * @property email E-mail, String.
 * @property password Password, String.
 * @property phone Phone number, String.
 * @property avatar Associated image or profile picture, String.
 * @property role Role, [UserRole].
 * @property createdAt Creation date, LocalDate.
 * @property active Logic and non-permanent elimination property, Boolean.
 */
@Table(name = "users")
data class User(
    @Id
    val id: UUID? = null,

    @NotEmpty(message = "El usuario debe tener un username.")
    @get:JvmName("userName")
    val username: String,

    @NotEmpty(message = "El usuario debe tener un email.")
    val email: String,

    @NotEmpty(message = "El usuario debe tener una password.")
    @get:JvmName("userPassword")
    val password: String,

    @NotEmpty(message = "El usuario debe tener un numero de telefono.")
    val phone: String,
    val avatar: String = "",
    @NotEmpty(message = "El usuario debe tener un rol.")
    val role: UserRole,
    @Column("created_at")
    val createdAt: LocalDate = LocalDate.now(),
    val active: Boolean
) : UserDetails {

    /**
     * Clase enum usada para los distintos roles de los usuarios
     * @author Mario Gonzalez, Daniel Rodriguez, Joan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     */
    enum class UserRole {
        USER, ADMIN, SUPER_ADMIN
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        val authority = SimpleGrantedAuthority("ROLE_${role.name}")
        return mutableListOf<GrantedAuthority>(authority)
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

}
