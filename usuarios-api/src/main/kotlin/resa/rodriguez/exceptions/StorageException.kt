package resa.rodriguez.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Custom exception for when an error related to the storage system happens.
 * @property message Error message.
 * @property cause full exception trace.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
sealed class StorageException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}

/**
 * Custom exception for when an error related to the storage system happens.
 * It returns a ResponseEntity with the status code 400 along with the error message.
 * Used for bad requests.
 * @property message Error message.
 * @property cause full exception trace.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class StorageExceptionBadRequest : StorageException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}

/**
 * Custom exception for when an error related to the storage system happens.
 * It returns a ResponseEntity with the status code 404 along with the error message.
 * Used for not founds.
 * @property message Error message.
 * @property cause full exception trace.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class StorageExceptionNotFound : StorageException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}