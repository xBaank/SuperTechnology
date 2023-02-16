package blanco.maldonado.mendoza.apiproductos.validator

import blanco.maldonado.mendoza.apiproductos.dto.ProductoCreateDto
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoBadRequestException

fun ProductoCreateDto.validate(): ProductoCreateDto {
    if (this.nombre.isBlank()) {
        throw ProductoBadRequestException("El nombre no puede estar vacío")
    } else if (this.categoria.toString().isBlank()) { //TODO: No funciona esta excepción
        throw ProductoBadRequestException("La categoría no puede estar vacía")
    } else if (this.stock < 0) {
        throw ProductoBadRequestException("El stock no puede ser negativo")
    } else if (this.description.isBlank()) {
        throw ProductoBadRequestException("La descripción no puede estar vacía ")
    } else if (this.precio <= 0) {
        throw ProductoBadRequestException("El precio no puede ser cero o negativo")
    }
    return this
}