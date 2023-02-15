package pedidosApi.dto.responses

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val id: String,
    val username: String,
    val email: String,
    val role: Role
)

enum class Role {
    ADMIN, USER, SUPER_ADMIN
}