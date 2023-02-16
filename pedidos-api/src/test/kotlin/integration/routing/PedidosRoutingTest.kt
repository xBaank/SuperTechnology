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
    fun `should get all pedidos by page`() = testApplication {
        val response = client.get("/pedidos?page=0&size=10")
        response.status.shouldBe(HttpStatusCode.OK)
    }

    @Test
    fun `should not get all pedidos by page`() = testApplication {
        val response = client.get("/pedidos?page=-5&size=10")
        response.status.shouldBe(HttpStatusCode.BadRequest)
    }

    @Test
    fun `should not get by incorrect id`() = testApplication {
        val response = client.get("/pedidos/fad")
        response.status.shouldBe(HttpStatusCode.BadRequest)
    }

    @Test
    fun `should not update by incorrect id`() = testApplication {
        val response = client.put("/pedidos/fad") {
            contentType(ContentType.Application.Json)
        }
        response.status.shouldBe(HttpStatusCode.BadRequest)
    }

    @Test
    fun `should create pedido`() = testApplication {
        val response = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.createPedido)
        }
        response.status.shouldBe(HttpStatusCode.Created)
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

        responsePost.status.shouldBe(HttpStatusCode.Created)
        responsePut.status.shouldBe(HttpStatusCode.OK)
    }

    @Test
    fun `should create pedido and then find it`() = testApplication {
        val responsePost = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.createPedido)
        }

        val pedidoCreated = Json.decodeFromString<PedidoDto>(responsePost.bodyAsText())

        val responsePut = client.get("/pedidos/${pedidoCreated.id}")

        responsePost.status.shouldBe(HttpStatusCode.Created)
        responsePut.status.shouldBe(HttpStatusCode.OK)
    }

    @Test
    fun `should create pedido and then find it by user id`() = testApplication {
        val responsePost = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.createPedido)
        }

        val pedidoCreated = Json.decodeFromString<PedidoDto>(responsePost.bodyAsText())

        val responseGet = client.get("/pedidos/usuario/${pedidoCreated.usuario.id}")

        responsePost.status.shouldBe(HttpStatusCode.Created)
        responseGet.status.shouldBe(HttpStatusCode.OK)
    }

    @Test
    fun `should create pedido and then not find it by user id paged`() = testApplication {
        val responsePost = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.createPedido)
        }

        val pedidoCreated = Json.decodeFromString<PedidoDto>(responsePost.bodyAsText())

        val responseGet = client.get("/pedidos/usuario/${pedidoCreated.usuario.id}?page=-5&size=10")

        responsePost.status.shouldBe(HttpStatusCode.Created)
        responseGet.status.shouldBe(HttpStatusCode.BadRequest)
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

        responsePost.status.shouldBe(HttpStatusCode.Created)
        responseDelete.status.shouldBe(HttpStatusCode.NoContent)
    }

    @Test
    fun `should not delete unknown pedido`() = testApplication {
        val responseDelete = client.delete("/pedidos/63ebb2569be16967bba54c3b")

        responseDelete.status.shouldBe(HttpStatusCode.NotFound)
    }
}