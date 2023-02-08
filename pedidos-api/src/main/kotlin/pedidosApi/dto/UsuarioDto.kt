package pedidosApi.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val id: String,
    val nombre: String,
    val email: String,
    val password: String
)