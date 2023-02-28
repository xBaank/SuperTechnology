package jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Component

private val algorithm: Algorithm = Algorithm.HMAC512("Zanahoria turbopropulsada")

@Component
class JwtTokensUtils {
    fun decode(token: String): DecodedJWT? {
        val verifier = JWT.require(algorithm)
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