package pedidosApi.dto

import kotlinx.serialization.Serializable

@Serializable
data class PedidoDto(
    val usuario: String,
    val productos: List<String>,
    val total: Double
)