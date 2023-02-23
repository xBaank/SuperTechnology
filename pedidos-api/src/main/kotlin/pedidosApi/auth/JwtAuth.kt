package pedidosApi.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm.HMAC256
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureAuth() = install(Authentication) {
    val secret = this@configureAuth.environment.config.property("jwt.secret").getString()
    val issuer = this@configureAuth.environment.config.property("jwt.issuer").getString()
    val audience = this@configureAuth.environment.config.property("jwt.audience").getString()

    val issuerSigned = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .sign(HMAC256(secret))

    jwt("admin") {
        verifier(issuerSigned)
        validate { credential ->
            if (credential.payload.getClaim("rol").asString() == "ADMIN") {
                JWTPrincipal(credential.payload)
            } else {
                null
            }
        }
    }

    jwt("user") {
        verifier(issuerSigned)
        validate { credential ->
            if (credential.payload.getClaim("rol").asString() == "USER") {
                JWTPrincipal(credential.payload)
            } else if (credential.payload.getClaim("rol").asString() == "ADMIN") {
                JWTPrincipal(credential.payload)
            } else {
                null
            }
        }
    }
}