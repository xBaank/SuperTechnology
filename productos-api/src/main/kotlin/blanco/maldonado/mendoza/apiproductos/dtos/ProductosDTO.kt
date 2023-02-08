package blanco.maldonado.mendoza.apiproductos.dtos

import blanco.maldonado.mendoza.apiproductos.model.Producto
import java.time.LocalDateTime
import java.util.*

data class ProductosDTO (
    val id: String,
    val nombre: String,
    val categoria: String,
    val stock: Int,
    val description: String,
    val createdAt: String,
    val updateAt: String,
    val deleteAt: String,
    val precio: Double,
    val activo: Boolean
){

}