package blanco.maldonado.mendoza.apiproductos.config.security.jwt

/**
 * @since 28/02/2023
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Jwt tokens utils: Class that generate the token with the secret passwords using HMAC512 encryption.
 */
@Component
class JwtTokensUtils {
    @Value("\${jwt.secret:Zanahoria_Turbopropulsada9}")
    private val algorithmSecret: String = "Zanahoria_Turbopropulsada9"

    /**
     * Decode: Decode the password
     *
     * @param token
     * @return
     */
    fun decode(token: String): DecodedJWT? {
        val verifier = JWT.require(Algorithm.HMAC512(algorithmSecret))
            .build()

        return try {
            verifier.verify(token)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Companion: allow us to obtain the fields in a simple way.
     */
    companion object {

        const val TOKEN_HEADER = "Authorization"
        const val TOKEN_PREFIX = "Bearer " // Importante
        const val TOKEN_TYPE = "JWT"
    }
}