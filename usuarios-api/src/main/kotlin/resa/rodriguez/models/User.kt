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
 * Modelo para usuarios
 * @property id Identificador principal, UUID
 * @property username Nombre de usuario, String
 * @property email Correo electronico del usuario, String
 * @property password Clave del usuario, String
 * @property phone Telefono del usuario, String
 * @property avatar Imagen asociada al usuario, String
 * @property role Rol del usuario, [UserRole]
 * @property createdAt Fecha de creacion del usuario, LocalDate
 * @property active Eliminacion logica y no definitiva del usuario, Boolean
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
     * Clase usado para los distintos roles de los usuarios
     */
    enum class UserRole {
        USER, ADMIN, SUPER_ADMIN
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        //val authority = SimpleGrantedAuthority("ROLE_${role.name}")
        //return mutableListOf<GrantedAuthority>(authority)
        return role.name.split(",").map { SimpleGrantedAuthority("ROLE_${it.trim()}") }.toMutableList()
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
