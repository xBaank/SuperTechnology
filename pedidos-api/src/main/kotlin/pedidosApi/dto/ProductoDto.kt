package pedidosApi.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductoDto(
    val id: String,
    val nombre: String,
    val categoria: String,
    val stock: Int,
    val descripcion: String,
    val precio: Double,
    val avatar: String
)