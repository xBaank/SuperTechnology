package pedidosApi

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import pedidosApi.auth.configureAuth
import pedidosApi.modules.mainModule
import pedidosApi.routing.pedidosRouting

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(Koin) {
        modules(mainModule)
    }
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
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
    install(CORS) {
        anyHost()
    }
    configureAuth()
    routing {
        pedidosRouting()
    }
}