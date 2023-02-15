package integration.routing

import integration.data.PedidosData
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import pedidosApi.dto.responses.PedidoDto


class PedidosRoutingTest {
    @JvmField
    val mongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:latest")).apply {
        start()
    }

    init {
        stopKoin()
        System.setProperty("mongo.connectionString", mongoDBContainer.connectionString)
        System.setProperty("mongo.database", "pedidos")
    }

    @Test
    fun `should get all pedidos`() = testApplication {
        val response = client.get("/pedidos")
        response.status.shouldBe(HttpStatusCode.OK)
    }

    @Test
    fun `should create pedido`() = testApplication {
        val response = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.createPedido)
        }
        response.status.shouldBe(HttpStatusCode.OK)
    }

    @Test
    fun `should not create pedido with incorrect body`() = testApplication {
        val response = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.incorrectFormat)
        }
        response.status.shouldBe(HttpStatusCode.BadRequest)
    }

    @Test
    fun `should create pedido and then update it`() = testApplication {
        val responsePost = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.createPedido)
        }

        val pedidoCreated = Json.decodeFromString<PedidoDto>(responsePost.bodyAsText())

        val responsePut = client.put("/pedidos/${pedidoCreated.id}") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.updatePedido)
        }

        responsePost.status.shouldBe(HttpStatusCode.OK)
        responsePut.status.shouldBe(HttpStatusCode.OK)
    }

    @Test
    fun `should not update unknown pedido`() = testApplication {
        val responsePut = client.put("/pedidos/63ebb2569be16967bba54c3b") {
            contentType(ContentType.Application.Json)
        }
        responsePut.status.shouldBe(HttpStatusCode.NotFound)
    }

    @Test
    fun `should create pedido and then delete it`() = testApplication {
        val responsePost = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.createPedido)
        }
        val pedidoCreated = Json.decodeFromString<PedidoDto>(responsePost.bodyAsText())

        val responseDelete = client.delete("/pedidos/${pedidoCreated.id}")

        responsePost.status.shouldBe(HttpStatusCode.OK)
        responseDelete.status.shouldBe(HttpStatusCode.NoContent)
    }

}