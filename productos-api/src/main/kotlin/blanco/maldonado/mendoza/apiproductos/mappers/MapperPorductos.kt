package blanco.maldonado.mendoza.apiproductos.mappers

import blanco.maldonado.mendoza.apiproductos.dtos.ProductosDTO
import blanco.maldonado.mendoza.apiproductos.model.Producto


fun Producto.toDto(producto: Producto) = ProductosDTO(
    id= producto.id.toString(),
    nombre= producto.nombre,
    categoria = producto.categoria.toString(),
    stock = producto.stock,
    description = producto.description,
    createdAt= producto.createdAt.toString(),
    updateAt = producto.updateAt.toString(),
    deleteAt = producto.deleteAt.toString(),
    precio = producto.precio,
    activo = producto.activo
)

fun ProductosDTO.toProducto(productoDTO: ProductosDTO) = Producto(
    id= productoDTO.id.toU,
    nombre= productoDTO.nombre,
    categoria = productoDTO.categoria,
    stock = productoDTO.stock,
    description = productoDTO.description,
    createdAt= productoDTO.createdAt,
    updateAt = productoDTO.updateAt,
    deleteAt = productoDTO.deleteAt,
    precio = productoDTO.precio,
    activo = productoDTO.activo
)

