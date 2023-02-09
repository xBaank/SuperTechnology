package pedidosApi.dto

import kotlinx.serialization.Serializable

@Serializable
data class PedidoDto(
    val id: String,
    val usuario: UsuarioDto,
    val tareas: List<TareaDto>,
)