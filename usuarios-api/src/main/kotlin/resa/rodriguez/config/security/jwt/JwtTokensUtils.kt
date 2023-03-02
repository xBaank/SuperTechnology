package resa.rodriguez.config.security.jwt

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Component
import resa.rodriguez.models.User
import java.util.*

private val algorithm: Algorithm = Algorithm.HMAC512("Zanahoria turbopropulsada")

/**
 * Contiene los métodos necesarios para la creación de tokens, comprobación y decodificación para verificarlos
 *
 */
@Component
class JwtTokensUtils {
    fun create(user: User): String {
        return JWT.create()
            .withSubject(user.id.toString())
            .withHeader(mapOf("typ" to TOKEN_TYPE))
            .withClaim("username", user.username)
            .withClaim("email", user.email)
            .withClaim("password", user.email)
            .withClaim("role", user.role.name)
            .withClaim("active", user.active)
            .withExpiresAt(Date(System.currentTimeMillis() + (24 * 60 * 60 * 1_000)))
            .sign(algorithm)
    }

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