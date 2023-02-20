package integration.data

import pedidosApi.dto.requests.CreatePedidoDto
import pedidosApi.dto.requests.CreateTareaDto
import pedidosApi.dto.requests.UpdatePedidoDto

object PedidosData {
    val createPedido = CreatePedidoDto(
        usuario = "fake",
        tareas = listOf(
            CreateTareaDto(
                producto = "fake",
                empleado = "fake"
            )
        ),
        iva = 0.21
    )

    val updatePedido = UpdatePedidoDto(
        iva = 0.21,
        estado = null
    )

    val incorrectFormat = "adsd"
}