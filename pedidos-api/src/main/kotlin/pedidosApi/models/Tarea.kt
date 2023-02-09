package pedidosApi.models

import org.litote.kmongo.Id
import org.litote.kmongo.newId
import pedidosApi.dto.ProductoDto
import pedidosApi.dto.UsuarioDto


data class Tarea(
    val _id: Id<String> = newId(),
    val productos: List<ProductoDto>,
    val empleado: UsuarioDto,
)

val Tarea.precio: Double get() = productos.sumOf(ProductoDto::precio)
val Tarea.cantidad: Int get() = productos.size