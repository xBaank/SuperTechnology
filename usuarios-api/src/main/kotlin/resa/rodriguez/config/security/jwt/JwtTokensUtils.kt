package resa.rodriguez.config.security.jwt

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import resa.rodriguez.dto.UserDTOresponse
import resa.rodriguez.mappers.UserMapper
import resa.rodriguez.models.User
import resa.rodriguez.models.UserRole
import resa.rodriguez.repositories.user.IUserRepositoryCached
import java.time.Instant
import java.util.*

private val algorithm: Algorithm = Algorithm.HMAC512("Zanahoria turbopropulsada")

/**
 * @author Daniel Rodriguez Muñoz
 * Contiene los métodos necesarios para la creación de tokens, comprobación y decodificación para verificarlos
 */
@Component
class JwtTokensUtils {
    fun create(user: User): String {
        return JWT.create()
            .withIssuer(user.id.toString())
            .withHeader(mapOf("typ" to TOKEN_TYPE))
            .withClaim("username", user.username)
            .withClaim("email", user.email)
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

    fun checkToken(token: String, role: UserRole): ResponseEntity<String>? {
        val decoded = decode(token)
            ?: return ResponseEntity("No token detected.", HttpStatus.UNAUTHORIZED)
        if (decoded.getClaim("role").isMissing || decoded.getClaim("active").isMissing ||
            decoded.getClaim("role").isNull || decoded.getClaim("active").isNull ||
            decoded.getClaim("active").asBoolean() == false
        )
            return ResponseEntity("Invalid token.", HttpStatus.UNAUTHORIZED)
        if (decoded.expiresAtAsInstant.isBefore(Instant.now()))
            return ResponseEntity("Token expired.", HttpStatus.UNAUTHORIZED)
        when (role) {
            UserRole.SUPER_ADMIN -> {
                if (!decoded.getClaim("role").asString().equals(UserRole.SUPER_ADMIN.name)) {
                    return ResponseEntity("You are not allowed to to this.", HttpStatus.FORBIDDEN)
                }
            }

            UserRole.ADMIN -> {
                if (!(decoded.getClaim("role").asString().equals(UserRole.SUPER_ADMIN.name) ||
                            decoded.getClaim("role").asString().equals(UserRole.ADMIN.name))
                ) {
                    return ResponseEntity("You are not allowed to to this.", HttpStatus.FORBIDDEN)
                }
            }

            UserRole.USER -> {}
        }
        return null
    }

    suspend fun getUserDTOFromToken(token: String, repo: IUserRepositoryCached, mapper: UserMapper): UserDTOresponse? {
        val decoded = decode(token) ?: return null
        return if (!decoded.issuer.isNullOrBlank()) {
            try {
                repo.findById(UUID.fromString(decoded.issuer))?.let { mapper.toDTO(it) }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    suspend fun getUserFromToken(token: String, repo: IUserRepositoryCached): User? {
        val decoded = decode(token) ?: return null

        return if (!decoded.issuer.isNullOrBlank()) {
            try {
                repo.findById(UUID.fromString(decoded.issuer))
            } catch (e: Exception) {
                null
            }
        } else null
    }

    // Nos permite obtener los campos importantes de manera sencilla
    companion object {

        const val TOKEN_HEADER = "Authorization"
        const val TOKEN_PREFIX = "Bearer " // Importante
        const val TOKEN_TYPE = "JWT"
    }
}