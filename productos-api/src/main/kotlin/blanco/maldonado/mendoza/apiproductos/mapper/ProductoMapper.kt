package blanco.maldonado.mendoza.apiproductos.mapper

import blanco.maldonado.mendoza.apiproductos.dto.ProductoCreateDto
import blanco.maldonado.mendoza.apiproductos.dto.ProductoDTO
import blanco.maldonado.mendoza.apiproductos.model.Producto

fun Producto.toDto() = ProductoDTO(
    id = this.id,
    uuid = this.uuid,
    nombre = this.nombre,
    categoria = this.categoria,
    stock = this.stock,
    description = this.description,
    precio = this.precio,
    activo = this.activo,
    metadata = ProductoDTO.MetaData(
        createdAt = this.createdAt.toString(),
        updateAt = this.updateAt.toString(),
        deleteAt = this.deleteAt.toString()
    )
)

fun ProductoCreateDto.toModel() = Producto(
    uuid = this.uuid,
    nombre = this.nombre,
    categoria = this.categoria,
    stock = this.stock,
    description = this.description,
    precio = this.precio,
    activo = this.activo
)