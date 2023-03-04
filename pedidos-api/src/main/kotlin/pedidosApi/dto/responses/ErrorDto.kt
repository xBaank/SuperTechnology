package pedidosApi.dto.responses

import kotlinx.serialization.Serializable

@Serializable
data class ErrorDto(
    val error: String,
    val code: Int
)