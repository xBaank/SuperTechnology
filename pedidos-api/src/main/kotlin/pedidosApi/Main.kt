package pedidosApi

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import pedidosApi.routing.pedidosRouting

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    configureContentNegotiation()
    configureSwagger()
    configureCORS()
    configureAuth()
    routing {
        pedidosRouting()
    }
}

private fun Application.configureKoin() {
    install(Koin) {
        modules(mainModule)
    }
}

private fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

private fun Application.configureSwagger() {
    install(SwaggerUI) {
        swagger {
            swaggerUrl = "docs"
            forwardRoot = true
        }
        info {
            title = "Pedidos API"
            version = "latest"
        }
        server {
            url = "http://localhost:8080"
            description = "Development Server"
        }
    }
}

private fun Application.configureCORS() {
    install(CORS) {
        anyHost()
    }
}

fun Application.configureAuth() = install(Authentication) {
    val secret = this@configureAuth.environment.config.property("jwt.secret").getString()
    val issuer = this@configureAuth.environment.config.property("jwt.issuer").getString()
    val audience = this@configureAuth.environment.config.property("jwt.audience").getString()

    val issuerSigned = JWT
        .require(Algorithm.HMAC512(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

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