/**
 * @since 16/02/2023
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
package blanco.maldonado.mendoza.apiproductos.mapper

import blanco.maldonado.mendoza.apiproductos.dto.ProductoCreateDto
import blanco.maldonado.mendoza.apiproductos.dto.ProductoDto
import blanco.maldonado.mendoza.apiproductos.dto.ProductoDtoUser
import blanco.maldonado.mendoza.apiproductos.model.Producto

/**
 * To dto: Esta función de extensión de Producto se ocupa de convertir el objeto de tipos transferencia de datos (DTO) para poder
 * pasar la información del mismo a ficheros de una forma más sencilla, evitando los tipos complejos.
 *
 */
fun Producto.toDto() = ProductoDto(
    uuid = this.uuid,
    nombre = this.nombre,
    categoria = this.categoria.toString(),
    stock = this.stock,
    description = this.description,
    precio = this.precio,
    activo = this.activo.toString(),
    createdAt = this.createdAt.toString(),
    updateAt = this.updateAt.toString(),
    deleteAt = this.deleteAt.toString()
)

fun Producto.toDtoUser() = ProductoDtoUser(
    uuid = this.uuid,
    nombre = this.nombre,
    categoria = this.categoria.toString(),
    stock = this.stock,
    description = this.description,
    precio = this.precio
)

/**
 * To model:
 *
 */
fun ProductoCreateDto.toModel() = Producto(
    nombre = this.nombre,
    categoria = Producto.Categoria.valueOf(this.categoria),
    stock = this.stock,
    description = this.description,
    precio = this.precio,
    activo = this.activo.toBoolean()
)