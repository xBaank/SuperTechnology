package pedidosApi.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreatePedidoDto(
    val usuario: String,
    val productos: List<String>,
    val iva: Double,
)