package blanco.maldonado.mendoza.apiproductos.validator

import blanco.maldonado.mendoza.apiproductos.dto.ProductoCreateDto
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoBadRequestExcepcion

fun ProductoCreateDto.validate(): ProductoCreateDto {
    if (this.nombre.isBlank()) {
        throw ProductoBadRequestExcepcion("El nombre no puede estar vacío")
    } else if (this.categoria.name.isBlank()) {
        throw ProductoBadRequestExcepcion("La categoría no puede estar vacía")
    } else if (this.stock < 0) {
        throw ProductoBadRequestExcepcion("El stock no puede ser negativo")
    } else if (this.description.isBlank()) {
        throw ProductoBadRequestExcepcion("La descripción no puede estar vacía ")
    } else if (this.precio < 0) {
        throw ProductoBadRequestExcepcion("El precio no puede ser negativo")
    }
    return this
}