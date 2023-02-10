package blanco.maldonado.mendoza.apiproductos.mappers

import blanco.maldonado.mendoza.apiproductos.dtos.ProductosDTO
import blanco.maldonado.mendoza.apiproductos.model.Producto
import java.time.LocalDateTime
import java.util.*


fun Producto.toDto(producto: Producto) = ProductosDTO(
    id = producto.id.toString(),
    nombre = producto.nombre,
    categoria = producto.categoria.toString(),
    stock = producto.stock,
    description = producto.description,
    createdAt = producto.createdAt.toString(),
    updateAt = producto.updateAt.toString(),
    deleteAt = producto.deleteAt.toString(),
    precio = producto.precio,
    activo = producto.activo
)

fun ProductosDTO.toProducto(productoDTO: ProductosDTO) = Producto(
    id = UUID.randomUUID(),
    nombre = productoDTO.nombre,
    categoria = Producto.Categoria.MONTAJE,
    stock = productoDTO.stock,
    description = productoDTO.description,
    createdAt = LocalDateTime.parse(this.createdAt),
    updateAt = LocalDateTime.parse(this.updateAt),
    deleteAt = LocalDateTime.parse(this.deleteAt),
    precio = productoDTO.precio,
    activo = productoDTO.activo
)