package pedidosApi.dto.responses

import kotlinx.serialization.Serializable

@Serializable
data class TareaDto(
    val id: String,
    val producto: ProductoDto,
    val empleado: UsuarioDto,
    val createdAt: Long
)

