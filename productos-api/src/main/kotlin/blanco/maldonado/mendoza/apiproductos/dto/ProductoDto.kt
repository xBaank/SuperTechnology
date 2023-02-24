/**
 * @since 16/02/2023
 * @author Azahara Blanco, Alfredo Maldonado, Sebastian Mendoza
 */
package blanco.maldonado.mendoza.apiproductos.dto

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

data class ProductoCreateDto(
    var nombre: String,
    val categoria: String,
    val stock: Int,
    val description: String,
    val precio: Double,
    val activo: String
)