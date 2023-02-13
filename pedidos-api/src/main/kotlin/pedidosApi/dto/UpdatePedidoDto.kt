package pedidosApi.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePedidoDto(
    val iva: Double? = null,
    val estado: String? = null,
)