package blanco.maldonado.mendoza.apiproductos.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * ProductosException
 * @param mensaje : String mensaje que da la excepcion
 */
sealed class ProductoException(mensage : String): RuntimeException(mensage)

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
class ProductoBadRequestExcepcion(mensage: String): ProductoException(mensage)

/**
 * Producto Conflictin integrity Exception
 * @param menjase: String
 * @see ProductoException
 * @see ResponseStatus
 * @see HttpStatus conflicting
 */
@ResponseStatus(HttpStatus.CONFLICT)
class  ProductoConflictIntegrutyException(mensage: String): ProductoException(mensage)