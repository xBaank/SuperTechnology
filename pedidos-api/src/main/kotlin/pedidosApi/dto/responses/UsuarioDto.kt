package pedidosApi.dto.responses

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val username: String,
    val email: String,
    val role: Role,
    val addresses: Set<String>,
    val avatar: String,
    val createdAt: String,
    val active: Boolean
)

@Serializable
enum class Role {
    ADMIN, USER, SUPER_ADMIN
}