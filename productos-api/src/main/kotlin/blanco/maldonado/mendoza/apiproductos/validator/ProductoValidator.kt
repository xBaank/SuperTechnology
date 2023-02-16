package blanco.maldonado.mendoza.apiproductos.validator

import blanco.maldonado.mendoza.apiproductos.dto.ProductoCreateDto
<<<<<<< HEAD
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
=======
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoBadRequestExcepcion
import blanco.maldonado.mendoza.apiproductos.model.Producto
import org.hibernate.validator.internal.constraintvalidators.hv.UUIDValidator
import java.util.*

fun ProductoCreateDto.validate(): ProductoCreateDto {
    if(this.uuid.trim().isBlank()){
        throw ProductoBadRequestExcepcion("El  uuid no puede estar vacío")
    }else {
        println("entra en validar el uuid")
        try {
            //todo falla aqui porque no sabemos como castear el uuid
            var uuidVerdadero = UUID.fromString(this.uuid.trim())
            println("entra en validar el pero se pasa el cath por ahí ")
        } catch (e: IllegalArgumentException) {
            println("entra en validar el uuid y ademas entra en el cath")
            throw ProductoBadRequestExcepcion("El Uuid no es un UUid valido")
        }
    }
    if (this.nombre.trim().isBlank()) {
        throw ProductoBadRequestExcepcion("El nombre no puede estar vacío")
    } else if (this.categoria.toString().trim().isBlank()) {
        throw ProductoBadRequestExcepcion("La categoría no puede estar vacía")
    }else {

        try {
            var categoriaCorrecta = Producto.Categoria.valueOf(categoria.trim())
        } catch (e: IllegalArgumentException) {
            throw ProductoBadRequestExcepcion("La categoría no es una categoria correcta")
        }

    }
    if (this.stock < 0) {
        throw ProductoBadRequestExcepcion("El stock no puede ser negativo")
    } else if (this.description.trim().isBlank()) {
        throw ProductoBadRequestExcepcion("La descripción no puede estar vacía ")
    } else if (this.precio <= 0) {
        throw ProductoBadRequestExcepcion("El precio no puede ser cero o negativo")
    }else if(this.activo.trim().equals("true")){

    }else if(this.activo.trim().equals("false")  ){

    }else{
        throw ProductoBadRequestExcepcion("La caracteristica activo solo puede ser true o false")
>>>>>>> dev-productos
    }
    return this
}