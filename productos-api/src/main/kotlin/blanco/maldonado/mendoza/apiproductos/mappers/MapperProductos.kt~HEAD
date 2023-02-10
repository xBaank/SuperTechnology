package blanco.maldonado.mendoza.apiproductos.mappers

import blanco.maldonado.mendoza.apiproductos.dtos.ProductosDTO
import blanco.maldonado.mendoza.apiproductos.model.Producto
import java.time.LocalDateTime
import java.util.*



fun Producto.toDto() = ProductosDTO(
    id= this.id.toString(),
    nombre= this.nombre,
    categoria =this.categoria.toString(),
    stock = this.stock,
    description = this.description,
    createdAt= this.createdAt.toString(),
    updateAt = this.updateAt.toString(),
    deleteAt = this.deleteAt.toString(),
    precio = this.precio,
    activo = this.activo
)

fun ProductosDTO.toProducto() = Producto(
    id = UUID.randomUUID(),
    nombre = this.nombre,
    categoria = Producto.Categoria.valueOf(this.categoria),
    stock = this.stock,
    description = this.description,
    createdAt = LocalDateTime.parse(this.createdAt),
    updateAt = LocalDateTime.parse(this.updateAt),
    deleteAt = LocalDateTime.parse(this.deleteAt),
    precio = this.precio,
    activo = this.activo
)


