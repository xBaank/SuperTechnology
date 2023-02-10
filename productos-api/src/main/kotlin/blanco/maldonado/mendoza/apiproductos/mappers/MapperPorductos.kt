package blanco.maldonado.mendoza.apiproductos.mappers

import blanco.maldonado.mendoza.apiproductos.dtos.ProductosDTO
import blanco.maldonado.mendoza.apiproductos.model.Producto


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


