package pedidosApi.models

import org.litote.kmongo.Id
import org.litote.kmongo.newId
import pedidosApi.dto.UsuarioDto

data class Pedido(
    val _id: Id<Pedido> = newId(),
    val usuario: UsuarioDto,
    val tareas: List<Tarea>,
)

val Pedido.total: Double get() = tareas.sumOf(Tarea::precio)