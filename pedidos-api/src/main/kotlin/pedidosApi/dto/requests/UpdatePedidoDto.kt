package pedidosApi.dto.requests

import kotlinx.serialization.Serializable
import pedidosApi.models.EstadoPedido

@Serializable
data class UpdatePedidoDto(
    val iva: Double? = null,
    val updatePedidoDto: UpdatePedidoDto? = null,
    val estado: EstadoPedido? = null,
)

