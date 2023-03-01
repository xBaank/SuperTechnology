package integration.data

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import pedidosApi.dto.requests.CreatePedidoDto
import pedidosApi.dto.requests.CreateTareaDto
import pedidosApi.dto.requests.UpdatePedidoDto

object PedidosData {
    val createPedido = CreatePedidoDto(
        usuario = "fake",
        tareas = listOf(
            CreateTareaDto(
                producto = "fake",
                empleado = "fake"
            )
        ),
        iva = 0.21
    )

    val updatePedido = UpdatePedidoDto(
        iva = 0.21,
        estado = null
    )

    val incorrectFormat = "adsd"

    val config = ApplicationConfig("application.conf")
    val audience = config.property("jwt.audience").getString()
    val issuer = config.property("jwt.issuer").getString()
    val secret = config.property("jwt.secret").getString()


    val token: String = JWT.create()
        .withClaim("rol", "ADMIN")
        .withAudience(audience)
        .withIssuer(issuer)
        .sign(Algorithm.HMAC256(secret))

}