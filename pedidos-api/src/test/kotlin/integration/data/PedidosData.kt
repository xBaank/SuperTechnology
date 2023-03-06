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
    val secret = config.getString("jwt.secret")

    val token: String = JWT.create()
        .withClaim("role", "SUPER_ADMIN")
        .sign(Algorithm.HMAC512(secret))

    //val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI0ZTVlNDE1Ni0wZDAzLTRkNDYtODVmYi05M2M4YjZjOTZhNzkiLCJwYXNzd29yZCI6InRlc3RAZXhhbXBsZS5jb20iLCJyb2xlIjoiU1VQRVJfQURNSU4iLCJhY3RpdmUiOnRydWUsImV4cCI6MTY3ODE4MDM1NSwiZW1haWwiOiJ0ZXN0QGV4YW1wbGUuY29tIiwidXNlcm5hbWUiOiJUZXN0X1VzZXIifQ.t6i0-Op76y6y3AE6ruEpFzAFc6vWEMX5bazJMZQwceM5ZWLv-W9zbFwkCu1rGsT7nchkIhdMq2gdF3CGH1YN3Q"

}