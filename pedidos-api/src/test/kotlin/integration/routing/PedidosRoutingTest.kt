package integration.routing

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import pedidosApi.dto.PedidoDto


class PedidosRoutingTest {
    @JvmField
    val mongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:latest")).apply {
        start()
    }

    init {
        System.setProperty("mongo.connectionString", mongoDBContainer.connectionString)
        System.setProperty("mongo.database", "pedidos")
    }

    @Test
    fun `should get all`() = testApplication {
        val response = client.get("/pedidos")
        response.status.shouldBe(HttpStatusCode.OK)
    }

    @Test
    fun `should create pedido`() = testApplication {
        val response = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "usuario": "fake",
                    "productos": ["fake"],
                    "iva": 21
                }
            """.trimIndent()
            )
        }
        response.status.shouldBe(HttpStatusCode.OK)
    }

    @Test
    fun `should not create pedido`() = testApplication {
        val response = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "usuario": "fake",
                    "productos": ["fake"],
                    "iva": 21 asd
                }
            """.trimIndent()
            )
        }
        response.status.shouldBe(HttpStatusCode.BadRequest)
    }

    @Test
    fun `should update pedido`() = testApplication {
        val responsePost = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "usuario": "fake",
                    "productos": ["fake"],
                    "iva": 21
                }
            """.trimIndent()
            )
        }

        val pedidoCreated = Json.decodeFromString<PedidoDto>(responsePost.bodyAsText())

        val responsePut = client.put("/pedidos/${pedidoCreated.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "estado": "COMPLETADO",
                    "iva": 21
                }
            """.trimIndent()
            )
        }

        responsePost.status.shouldBe(HttpStatusCode.OK)
        responsePut.status.shouldBe(HttpStatusCode.OK)
    }

    @Test
    fun `should not update pedido`() = testApplication {
        val responsePut = client.put("/pedidos/63ebb2569be16967bba54c3b") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "estado": "COMPLETADO",
                    "iva": 21 asd
                }
            """.trimIndent()
            )
        }
        responsePut.status.shouldBe(HttpStatusCode.NotFound)
    }

    @Test
    fun `should delete pedido`() = testApplication {
        val responsePost = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "usuario": "fake",
                    "productos": ["fake"],
                    "iva": 21
                }
            """.trimIndent()
            )
        }
        val pedidoCreated = Json.decodeFromString<PedidoDto>(responsePost.bodyAsText())

        val responseDelete = client.delete("/pedidos/${pedidoCreated.id}")

        responsePost.status.shouldBe(HttpStatusCode.OK)
        responseDelete.status.shouldBe(HttpStatusCode.NoContent)
    }

}