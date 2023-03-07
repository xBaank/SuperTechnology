/**
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @since 28/2/2023
 */
package blanco.maldonado.mendoza.apiproductos.user

import jakarta.validation.constraints.NotEmpty
import org.springframework.data.annotation.Id
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

data class User(
    @Id
    val id: UUID? = null,

    @NotEmpty(message = "El usuario debe tener un username.")
    @get:JvmName("userName")
    val username: String,

    @NotEmpty(message = "El usuario debe tener un email.")
    val email: String,

    @NotEmpty(message = "El usuario debe tener un rol.")
    val role: UserRole,
    val active: Boolean
) : UserDetails {

    enum class UserRole {
        USER, ADMIN, SUPER_ADMIN
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return role.name.split(",").map { SimpleGrantedAuthority("ROLE_${it.trim()}") }.toMutableList()
    }

    override fun getPassword(): String {
        return "a"
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