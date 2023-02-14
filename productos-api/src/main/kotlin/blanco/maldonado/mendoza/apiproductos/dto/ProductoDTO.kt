package blanco.maldonado.mendoza.apiproductos.dto

import blanco.maldonado.mendoza.apiproductos.model.Producto
import java.time.LocalDateTime
import java.util.UUID

data class ProductoDTO(
    val id: Int? = null,
    val uuid: String? = null,
    val nombre: String,
    val categoria: String,
    val stock: Int,
    val description: String,
    val precio: Double,
    val activo: Boolean,
    val metadata: MetaData? = null
) {
    data class MetaData(
        val createdAt: String = LocalDateTime.now().toString(),
        val updateAt: String? = null,
        val deleteAt: String? = null
    )
}

data class ProductoCreateDto(
    val uuid: String = UUID.randomUUID().toString(),
    val nombre: String,
    val categoria: String,
    val stock: Int,
    val description: String,
    val precio: Double,
    val activo: Boolean
)