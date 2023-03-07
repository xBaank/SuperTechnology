/**
 * @since 16/02/2023
 * @author Azahara Blanco, Alfredo Maldonado, Sebastian Mendoza
 */
package blanco.maldonado.mendoza.apiproductos.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * ProductosException
 * @param mensaje : String mensaje que da la excepcion
 */
sealed class ProductoException(mensaje : String): RuntimeException(mensaje)

/**
 * Producto Not Found Exception
 * @param menjase: String
 * @see ProductoException
 * @see ResponseStatus
 * @see HttpStatus not_foun
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ProductoNotFoundException(mensaje: String): ProductoException(mensaje)

/**
 * Producto Bad Recuest Exception
 * @param menjase: String
 * @see ProductoException
 * @see ResponseStatus
 * @see HttpStatus bad_recuest
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class ProductoBadRequestException(mensaje: String): ProductoException(mensaje)

/**
 * Producto Conflictin integrity Exception
 * @param menjase: String
 * @see ProductoException
 * @see ResponseStatus
 * @see HttpStatus conflicting
 */
@ResponseStatus(HttpStatus.CONFLICT)
class  ProductoConflictIntegrityException(mensaje: String): ProductoException(mensaje)