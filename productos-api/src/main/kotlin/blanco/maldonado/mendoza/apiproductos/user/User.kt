package blanco.maldonado.mendoza.apiproductos.user

import jakarta.validation.constraints.NotEmpty
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

class User (
    val id: Long? = null,
    val uuid: UUID? = UUID.randomUUID(),
    @NotEmpty(message = "Must have a username.")
    @get:JvmName("userName")
    val username: String,
    @NotEmpty(message = "Must have an email.")
    val email: String,
    @NotEmpty(message = "Must have a password.")
    @get:JvmName("userPassword")
    val password: String,
    @NotEmpty(message = "Must have a rol.")
    val role: Role,
) : UserDetails {
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
enum class Role {
    EMPLEADO, ADMIN
}