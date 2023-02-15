package integration.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pedidosApi.dto.CreatePedidoDto
import pedidosApi.dto.CreateTareaDto
import pedidosApi.dto.UpdatePedidoDto

object PedidosData {
    val createPedido = Json.encodeToString(
        CreatePedidoDto(
            usuario = "fake",
            tareas = listOf(
                CreateTareaDto(
                    producto = "fake",
                    empleado = "fake"
                )
            ),
            iva = 0.21
        )
    )

    val updatePedido = Json.encodeToString(
        UpdatePedidoDto(
            iva = 0.21,
            estado = null
        )
    )

    val incorrectFormat = "adsd"
}