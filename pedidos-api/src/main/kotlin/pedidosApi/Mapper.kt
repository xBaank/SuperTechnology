package pedidosApi

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import pedidosApi.dto.responses.ErrorDto
import pedidosApi.dto.responses.PagedFlowDto
import pedidosApi.dto.responses.PedidoDto
import pedidosApi.dto.responses.TareaDto
import pedidosApi.models.Pedido
import pedidosApi.models.Tarea
import pedidosApi.repositories.PagedFlow

fun buildPedidoDto(pedido: Pedido) = PedidoDto(
    id = pedido._id.toString(),
    usuario = pedido.usuario,
    tareas = pedido.tareas.map(::buildTareaDto),
    iva = pedido.iva,
    estado = pedido.estado,
    createdAt = pedido.createdAt
)

fun buildTareaDto(tarea: Tarea) = TareaDto(
    id = tarea._id.toString(),
    producto = tarea.producto,
    empleado = tarea.empleado,
    createdAt = tarea.createdAt
)


suspend fun buildPagedPedidoDto(pedidos: PagedFlow<Pedido>): PagedFlowDto<PedidoDto> {
    val result = pedidos.map(::buildPedidoDto).toList()
    return PagedFlowDto(pedidos.page, result.size.toLong(), result)
}

fun buildErrorDto(error: String, code: Int) = ErrorDto(error, code)