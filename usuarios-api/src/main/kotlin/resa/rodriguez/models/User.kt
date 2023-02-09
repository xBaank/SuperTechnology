package resa.rodriguez.models

import jakarta.validation.constraints.NotEmpty
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.util.UUID

@Table(name = "users")
data class User (
    @Id
    val id: UUID,
    @NotEmpty(message = "El usuario debe tener un username.")
    val username: String,
    @NotEmpty(message = "El usuario debe tener un email.")
    val email: String,
    @NotEmpty(message = "El usuario debe tener una password.")
    val password: String,
    @NotEmpty(message = "El usuario debe tener un numero de telefono.")
    val phone: String,
    val avatar: String = "",
    @NotEmpty(message = "El usuario debe tener un rol.")
    val role: UserRole,
    @Column("created_at")
    val createdAt: LocalDate = LocalDate.now(),
    val activo: Boolean
)

enum class UserRole {
    USER,ADMIN,SUPER_ADMIN
}