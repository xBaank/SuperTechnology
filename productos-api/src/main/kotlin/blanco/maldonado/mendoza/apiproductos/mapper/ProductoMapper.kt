package blanco.maldonado.mendoza.apiproductos.mapper

import blanco.maldonado.mendoza.apiproductos.dto.ProductoCreateDto
import blanco.maldonado.mendoza.apiproductos.dto.ProductoDto
import blanco.maldonado.mendoza.apiproductos.model.Producto

fun Producto.toDto() = ProductoDto(
    id = this.id,
    nombre = this.nombre,
    categoria = this.categoria.toString(),
    stock = this.stock,
    description = this.description,
    precio = this.precio,
    activo = this.activo.toString(),
    metadata = ProductoDto.MetaData(
        createdAt = this.createdAt.toString(),
        updateAt = this.updateAt.toString(),
        deleteAt = this.deleteAt.toString()
    )
)

fun ProductoCreateDto.toModel() = Producto(
    id = this.id!!,
    nombre = this.nombre,
    categoria = Producto.Categoria.valueOf(this.categoria),
    stock = this.stock,
    description = this.description,
    precio = this.precio,
    activo = this.activo.toBoolean()
)