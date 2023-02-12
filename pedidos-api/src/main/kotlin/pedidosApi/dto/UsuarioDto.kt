package pedidosApi.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val id: String,
    val username: String,
    val email: String
)