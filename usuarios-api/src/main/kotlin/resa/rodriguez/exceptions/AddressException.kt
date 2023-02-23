package resa.rodriguez.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

sealed class AddressException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class AddressExceptionBadRequest : AddressException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}


@ResponseStatus(HttpStatus.NOT_FOUND)
class AddressExceptionNotFound : AddressException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}