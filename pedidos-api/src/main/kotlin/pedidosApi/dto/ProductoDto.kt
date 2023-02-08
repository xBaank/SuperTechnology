package pedidosApi.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductoDto(
    val id: String,
    val nombre: String,
    val precio: Double,
    val stock: Int
)