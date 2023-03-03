package integration.data

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
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

    val config = ConfigFactory.load("application.conf")
    val audience = config.getString("jwt.audience")
    val issuer = config.getString("jwt.issuer")
    val secret = config.getString("jwt.secret")

    val token: String = JWT.create()
        .withClaim("rol", "ADMIN")
        .withAudience(audience)
        .withIssuer(issuer)
        .sign(Algorithm.HMAC256(secret))

}