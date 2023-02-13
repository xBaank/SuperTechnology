package blanco.maldonado.mendoza.apiproductos.dto

import blanco.maldonado.mendoza.apiproductos.model.Producto
import java.time.LocalDateTime
import java.util.UUID

data class ProductoDTO(
    val id: UUID? = null,
    val nombre: String,
    val categoria: Producto.Categoria,
    val stock: Int,
    val description: String,
    val precio: Double,
    val activo: Boolean,
    val metadata: MetaData? = null
) {
    data class MetaData(
        val createdAt: String = LocalDateTime.now().toString(),
        val updateAt: String = LocalDateTime.now().toString(),
        val deleteAt: String? = null
    )
}

data class ProductoCreateDto(
    val nombre: String,
    val categoria: Producto.Categoria,
    val stock: Int,
    val description: String,
    val precio: Double,
    val activo: Boolean
)