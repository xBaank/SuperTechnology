package resa.rodriguez.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Custom exception for when an error related to addresses happens.
 * @property message Error message.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
sealed class AddressException(message: String?) : RuntimeException(message)

/**
 * Custom exception for when an error related to addresses happens.
 * It returns a ResponseEntity with the status code 400 along with the error message.
 * Used for bad requests.
 * @property message Error message.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class AddressExceptionBadRequest(message: String?) : UserException(message)

/**
 * Custom exception for when an error related to addresses happens.
 * It returns a ResponseEntity with the status code 404 along with the error message.
 * Used for not founds.
 * @property message Error message.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class AddressExceptionNotFound(message: String?) : AddressException(message)