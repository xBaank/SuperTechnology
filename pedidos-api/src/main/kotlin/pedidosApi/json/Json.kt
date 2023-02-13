package pedidosApi.json

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.*
import pedidosApi.dto.PedidoDto
import pedidosApi.dto.TareaDto
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
    productos = tarea.productos,
    empleado = tarea.empleado,
    createdAt = tarea.createdAt
)


suspend fun buildPagedPedidoJson(pedidos: PagedFlow<Pedido>) = buildJsonObject {
    val pedidosList = pedidos.map(::buildPedidoDto).toList()
    put("page", pedidos.page)
    put("size", pedidosList.size)
    put("result", Json.encodeToJsonElement(pedidosList))
}

fun buildErrorJson(error: String, code: Int) = buildJsonObject {
    put("error", JsonPrimitive(error))
    put("code", code)
}