package blanco.maldonado.mendoza.apiproductos.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

sealed class TokenException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class TokenInvalidException(message: String) : TokenException(message)