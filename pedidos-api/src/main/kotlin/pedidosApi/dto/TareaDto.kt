package pedidosApi.dto

import kotlinx.serialization.Serializable

@Serializable
data class TareaDto(
    val id: String,
    val productos: List<ProductoDto>,
    val empleado: UsuarioDto,
)