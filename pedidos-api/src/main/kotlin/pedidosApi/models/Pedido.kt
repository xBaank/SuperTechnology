package pedidosApi.models

import arrow.core.NonEmptyList
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import pedidosApi.dto.UsuarioDto

data class Pedido(
    val _id: Id<String> = newId(),
    val usuario: UsuarioDto,
    val tareas: List<Tarea>,
)
    val tareas: NonEmptyList<Tarea>,
    val estado: EstadoPedido
) {
    enum class EstadoPedido {
        ENTREGADO,
        EN_PROCESO,
        CANCELADO
    }
}

val Pedido.total: Double get() = tareas.sumOf(Tarea::precio)