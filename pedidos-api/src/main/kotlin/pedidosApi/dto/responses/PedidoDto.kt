package pedidosApi.dto.responses

import kotlinx.serialization.Serializable
import pedidosApi.models.EstadoPedido

@Serializable
data class PedidoDto(
    val id: String,
    val usuario: UsuarioDto,
    val tareas: List<TareaDto>,
    val iva: Double,
    val estado: EstadoPedido,
    val createdAt: Long
)