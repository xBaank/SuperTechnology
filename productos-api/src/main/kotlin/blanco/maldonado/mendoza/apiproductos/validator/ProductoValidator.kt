/**
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @since 13/2/2023
 */
package blanco.maldonado.mendoza.apiproductos.validator

import blanco.maldonado.mendoza.apiproductos.dto.ProductoCreateDto
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoBadRequestException
import blanco.maldonado.mendoza.apiproductos.model.Producto

fun ProductoCreateDto.validate(): ProductoCreateDto {

    if (this.nombre.trim().isBlank()) {
        throw ProductoBadRequestException("El nombre no puede estar vacío")
    } else if (this.categoria.trim().isBlank()) {
        throw ProductoBadRequestException("La categoría no puede estar vacía")
    } else {
        try {
            Producto.Categoria.valueOf(categoria.trim())
        } catch (e: IllegalArgumentException) {
            throw ProductoBadRequestException("La categoría no es una categoria correcta")
        }
    }
    if (this.stock < 0) {
        throw ProductoBadRequestException("El stock no puede ser negativo")
    } else if (this.description.trim().isBlank()) {
        throw ProductoBadRequestException("La descripción no puede estar vacía")
    } else if (this.precio <= 0) {
        throw ProductoBadRequestException("El precio no puede ser cero o negativo")
    } else if (this.activo.trim() != "false" && this.activo.trim() != "true") {
        throw ProductoBadRequestException("La característica activo solo puede ser true o false y estar en minúsculas")
    }
    return this
}