package pedidosApi.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductoDto(
    val id: String,
    val nombre: String,
    val categoria: Categoria,
    val stock: Int,
    val descripcion: String,
    val precio: Double,
    val avatar: String
)

enum class Categoria {
    COMPONENTES, DISPOSITIVO, MONTAJE, REPARACION, ACTUALIZACION
}