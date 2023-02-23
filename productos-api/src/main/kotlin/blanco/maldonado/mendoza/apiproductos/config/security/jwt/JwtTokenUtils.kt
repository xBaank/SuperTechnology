package blanco.maldonado.mendoza.apiproductos.config.security.jwt

import blanco.maldonado.mendoza.apiproductos.exceptions.TokenInvalidException
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

private val logger = KotlinLogging.logger { }

@Component
class JwtTokenUtils {
    @Value("\${jwt.secret:Zanahora turbopropulsada}")
    private val jwtSecreto: String? = null

    fun validarToken(token: String): DecodedJWT? {
        logger.info { "Validando el token: $token" }
        try {
            return JWT.require(Algorithm.HMAC512(jwtSecreto)).build().verify(token)
        } catch (e: Exception) {
            throw TokenInvalidException("Token no válido o expirado")
        }
    }

    fun getUsuarioIdFromJwt(token: String?): String {
        logger.info { "Obteniendo el Id del usuario: $token" }
        return validarToken(token!!)!!.subject
    }

    private fun getClaimsFromJwt(token: String) = validarToken(token)?.claims

    fun getRolesFromJwt(token: String): String {
        val claims = getClaimsFromJwt(token)
        return claims!!["rol"]!!.asString()
    }

    fun isTokenValid(token: String): Boolean {
        logger.info { "Comprobando si el token es válido: $token" }
        val claims = getClaimsFromJwt(token)!!
        val fechaExp = claims["exp"]!!.asDate()
        val now = Date(System.currentTimeMillis())
        return now.before(fechaExp)
    }

    companion object {
        const val TOKEN_HEADER = "Authorization"
        const val TOKEN_PREFIX = "Bearer "
        const val TOKEN_TYPE = "JWT"
    }
}