package pedidosApi.models

import org.litote.kmongo.Id
import org.litote.kmongo.newId
import pedidosApi.dto.responses.UsuarioDto

data class Pedido(
    val _id: Id<Pedido> = newId(),
    val usuario: UsuarioDto,
    val tareas: List<Tarea>,
    val iva: Double,
    val estado: EstadoPedido,
    val createdAt: Long
)

val Pedido.total: Double get() = tareas.sumOf(Tarea::precio)

enum class EstadoPedido {
    ENTREGADO,
    EN_PROCESO,
    CANCELADO
}