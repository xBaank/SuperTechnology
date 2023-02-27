package resa.rodriguez.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

sealed class AddressException(message: String?) : RuntimeException(message)

@ResponseStatus(HttpStatus.NOT_FOUND)
class AddressExceptionNotFound(message: String?) : AddressException(message)