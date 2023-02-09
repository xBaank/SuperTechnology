package pedidosApi.extensions

import pedidosApi.dto.PedidoDto
import pedidosApi.dto.TareaDto
import pedidosApi.models.Pedido
import pedidosApi.models.Tarea

fun Pedido.toDto() = PedidoDto(
    id = _id.toString(),
    usuario = usuario,
    tareas = tareas.map(Tarea::toDto)
)

fun Tarea.toDto() = TareaDto(
    id = _id.toString(),
    productos = productos,
    empleado = empleado
)