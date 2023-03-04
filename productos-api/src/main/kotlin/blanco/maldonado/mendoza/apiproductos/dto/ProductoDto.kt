/**
 * @since 16/02/2023
 * @author Azahara Blanco, Alfredo Maldonado, Sebastian Mendoza
 */
package blanco.maldonado.mendoza.apiproductos.dto

import java.time.LocalDateTime

data class ProductoDto(
    val uuid: String? = null,
    val nombre: String,
    val categoria: String,
    val stock: Int,
    val description: String,
    val precio: Double,
    val activo: String,
    val createdAt: String? = null,
    val updateAt: String? = null,
    val deleteAt: String? = null
)

data class ProductoPageDto(
    val content: List<ProductoDto>,
    val currentPage: Int,
    val pageSize: Int,
    val totalPages: Long,
    val totalProductos: Long,
    val createdAt: String = LocalDateTime.now().toString()
)

data class ProductoCreateDto(
    val nombre: String,
    val categoria: String,
    val stock: Int,
    val description: String,
    val precio: Double,
    val activo: String
)