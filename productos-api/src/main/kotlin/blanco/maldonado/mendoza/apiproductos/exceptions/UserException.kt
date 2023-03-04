package blanco.maldonado.mendoza.apiproductos.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

sealed class UserException(message: String?) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class UserExceptionBadRequest(message: String?) : UserException(message)

@ResponseStatus(HttpStatus.NOT_FOUND)
class UserExceptionNotFound(message: String?) : UserException(message)