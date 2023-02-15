package pedidosApi.models

import org.litote.kmongo.Id
import org.litote.kmongo.newId
import pedidosApi.dto.ProductoDto
import pedidosApi.dto.UsuarioDto


data class Tarea(
    val _id: Id<Tarea> = newId(),
    val producto: ProductoDto,
    val empleado: UsuarioDto,
    val createdAt: Long
)

val Tarea.precio: Double get() = producto.precio