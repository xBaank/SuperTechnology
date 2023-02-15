package pedidosApi.dto

import kotlinx.serialization.Serializable
import pedidosApi.models.EstadoPedido

@Serializable
data class UpdatePedidoDto(
    val iva: Double? = null,
    val updatePedidoDto: UpdatePedidoDto? = null,
    val estado: EstadoPedido? = null,
)

@Serializable
data class UpdateTareaDto(
    val producto: String? = null,
    val empleado: String? = null,
)