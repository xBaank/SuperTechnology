package pedidosApi.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreatePedidoDto(
    val usuarioUsername: String,
    val tareas: List<CreateTareaDto>,
    val iva: Double,
)

@Serializable
data class CreateTareaDto(
    val producto: String,
    val empleadoUsername: String
)