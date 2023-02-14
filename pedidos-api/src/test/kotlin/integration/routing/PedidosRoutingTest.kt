package integration.routing

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName


class PedidosRoutingTest {
    @JvmField
    val mongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:latest")).apply {
        withExposedPorts(27017)
        start()
    }

    init {
        System.setProperty("mongo.connectionString", mongoDBContainer.connectionString)
        System.setProperty("mongo.database", "pedidos")
    }

    @Test
    fun `should get all pedidos`() = testApplication {
        val response = client.get("/pedidos")
        response.status.shouldBe(HttpStatusCode.OK)
    }

    @Test
    fun `should post pedido`() = testApplication {
        val response = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "usuarioId": "fake",
                    "productos": ["fake"]
                }
            """.trimIndent()
            )
        }
        val body = response.bodyAsText()
        println(body)
        response.status.shouldBe(HttpStatusCode.OK)
    }
}