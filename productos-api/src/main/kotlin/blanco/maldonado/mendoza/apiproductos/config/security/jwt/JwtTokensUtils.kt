package blanco.maldonado.mendoza.apiproductos.config.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JwtTokensUtils {
    @Value("\${jwt.secret:Zanahoria_Turbopropulsada9}")
    private val algorithmSecret: String = "Zanahoria_Turbopropulsada9"

    fun decode(token: String): DecodedJWT? {
        val verifier = JWT.require(Algorithm.HMAC512(algorithmSecret))
            .build()

        return try {
            verifier.verify(token)
        } catch (_: Exception) {
            null
        }
    }

    // Nos permite obtener los campos importantes de manera sencilla
    companion object {

        const val TOKEN_HEADER = "Authorization"
        const val TOKEN_PREFIX = "Bearer " // Importante
        const val TOKEN_TYPE = "JWT"
    }
}