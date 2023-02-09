package pedidosApi.models

import arrow.core.NonEmptyList
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import pedidosApi.dto.UsuarioDto

data class Pedido(
    val _id: Id<String> = newId(),
    val usuario: UsuarioDto,
    val tareas: NonEmptyList<Tarea>,
)

val Pedido.total: Double get() = tareas.sumOf(Tarea::precio)