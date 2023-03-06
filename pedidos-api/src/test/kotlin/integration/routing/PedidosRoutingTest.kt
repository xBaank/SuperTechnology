package integration.routing

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import integration.data.PedidosData
import integration.data.testModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import pedidosApi.dto.responses.ErrorDto
import pedidosApi.dto.responses.PagedFlowDto
import pedidosApi.dto.responses.PedidoDto
import pedidosApi.models.EstadoPedido
import pedidosApi.module


@Testcontainers
class PedidosRoutingTest {

    companion object {
        @Container
        @JvmField
        val mongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:latest"))
    }

    @AfterEach
    fun tearDown() {
        mongoDBContainer.stop()
    }

    @BeforeEach
    fun setUp() {
        mongoDBContainer.start()
        stopKoin()
    }

    private fun ApplicationTestBuilder.createJsonClient(): HttpClient = createClient {
        this@createJsonClient.environment {
            config = HoconApplicationConfig(
                PedidosData.config
                    .withValue("mongo.connectionString", fromAnyRef(mongoDBContainer.connectionString))
                    .withValue("mongo.database", fromAnyRef("pedidos"))
            )
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        module = testModule
    }

    @Test
    fun `should create pedido and get all pedidos`() = testApplication {
        val jsonClient = createJsonClient()

        val responsePost = jsonClient.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.createPedido)
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        val response = jsonClient.get("/pedidos") {
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        val result = response.body<PagedFlowDto<PedidoDto>>()

        responsePost.status.shouldBe(HttpStatusCode.Created)
        response.status.shouldBe(HttpStatusCode.OK)
        result.size.shouldBeEqualTo(1)
        result.result.first().iva.shouldBeEqualTo(PedidosData.createPedido.iva)
        result.result.first().tareas.size.shouldBeEqualTo(PedidosData.createPedido.tareas.size)
    }

    @Test
    fun `should get all pedidos by page`() = testApplication {
        val jsonClient = createJsonClient()

        val response = jsonClient.get("/pedidos?page=0&size=10") {
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }
        response.status.shouldBe(HttpStatusCode.OK)

        val result = response.body<PagedFlowDto<PedidoDto>>()
        result.size.shouldBeEqualTo(0) //Real size, not the one specified in the request
        result.page.shouldBeEqualTo(0)
    }

    @Test
    fun `should not get all pedidos by page`() = testApplication {
        val jsonClient = createJsonClient()

        val response = jsonClient.get("/pedidos?page=-5&size=10") {
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }
        response.status.shouldBe(HttpStatusCode.BadRequest)

        val result = response.body<ErrorDto>()
        result.code.shouldBeEqualTo(400)
    }

    @Test
    fun `should not get by incorrect id`() = testApplication {
        val jsonClient = createJsonClient()

        val response = jsonClient.get("/pedidos/fad") {
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }
        response.status.shouldBe(HttpStatusCode.BadRequest)

        val result = response.body<ErrorDto>()
        result.code.shouldBeEqualTo(400)
    }

    @Test
    fun `should not update by incorrect id`() = testApplication {
        val jsonClient = createJsonClient()

        val response = jsonClient.put("/pedidos/fad") {
            contentType(ContentType.Application.Json)
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        val result = response.body<ErrorDto>()

        response.status.shouldBe(HttpStatusCode.BadRequest)
        result.code.shouldBeEqualTo(400)
    }

    @Test
    fun `should create pedido`() = testApplication {
        val jsonClient = createJsonClient()

        val response = jsonClient.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.createPedido)
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        val result = response.body<PedidoDto>()

        response.status.shouldBe(HttpStatusCode.Created)
        result.iva.shouldBeEqualTo(PedidosData.createPedido.iva)
        result.tareas.size.shouldBeEqualTo(PedidosData.createPedido.tareas.size)
    }

    @Test
    fun `should not create pedido with incorrect body`() = testApplication {
        val jsonClient = createJsonClient()

        val response = jsonClient.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.incorrectFormat)
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        val result = response.body<ErrorDto>()

        response.status.shouldBe(HttpStatusCode.BadRequest)
        result.code.shouldBeEqualTo(400)
    }

    @Test
    fun `should create pedido and then update it`() = testApplication {
        val jsonClient = createJsonClient()

        val responsePost = jsonClient.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.createPedido)
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        val pedidoCreated = responsePost.body<PedidoDto>()

        val responsePut = jsonClient.put("/pedidos/${pedidoCreated.id}") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.updatePedido)
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        val pedidoUpdated = responsePut.body<PedidoDto>()

        responsePost.status.shouldBe(HttpStatusCode.Created)
        responsePut.status.shouldBe(HttpStatusCode.OK)
        pedidoCreated.iva.shouldBeEqualTo(PedidosData.createPedido.iva)
        pedidoCreated.tareas.size.shouldBeEqualTo(PedidosData.createPedido.tareas.size)
        pedidoUpdated.iva.shouldBeEqualTo(PedidosData.updatePedido.iva)
        pedidoUpdated.estado.shouldBeEqualTo(EstadoPedido.EN_PROCESO)
    }

    @Test
    fun `should create pedido and then find it`() = testApplication {
        val jsonClient = createJsonClient()

        val responsePost = jsonClient.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.createPedido)
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        val pedidoCreated = responsePost.body<PedidoDto>()

        val responseGet = jsonClient.get("/pedidos/${pedidoCreated.id}") {
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        val pedidoFound = responseGet.body<PedidoDto>()

        responsePost.status.shouldBe(HttpStatusCode.Created)
        responseGet.status.shouldBe(HttpStatusCode.OK)
        pedidoCreated.shouldBeEqualTo(pedidoFound)
    }

    @Test
    fun `should create pedido and then find it by user id`() = testApplication {
        val jsonClient = createJsonClient()

        val responsePost = jsonClient.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.createPedido)
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        val pedidoCreated = responsePost.body<PedidoDto>()

        val responseGet = jsonClient.get("/pedidos/usuario/${pedidoCreated.usuario.id}") {
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        val pedidosFound = responseGet.body<PagedFlowDto<PedidoDto>>()

        responsePost.status.shouldBe(HttpStatusCode.Created)
        responseGet.status.shouldBe(HttpStatusCode.OK)
        pedidoCreated.shouldBeEqualTo(pedidosFound.result.first())
    }

    @Test
    fun `should create pedido and then not find it by user id paged`() = testApplication {
        val jsonClient = createJsonClient()

        val responsePost = jsonClient.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(PedidosData.createPedido)
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        val pedidoCreated = responsePost.body<PedidoDto>()

        val responseGet = jsonClient.get("/pedidos/usuario/${pedidoCreated.usuario.id}?page=-5&size=10") {
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        val pedidoFound = responseGet.body<ErrorDto>()

        responsePost.status.shouldBe(HttpStatusCode.Created)
        responseGet.status.shouldBe(HttpStatusCode.BadRequest)
        pedidoFound.code.shouldBeEqualTo(400)
    }

    @Test
    fun `should not update unknown pedido`() = testApplication {
        val jsonClient = createJsonClient()

        val responsePut = jsonClient.put("/pedidos/63ebb2569be16967bba54c3b") {
            contentType(ContentType.Application.Json)
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }
        val result = responsePut.body<ErrorDto>()
        responsePut.status.shouldBe(HttpStatusCode.NotFound)
        result.code.shouldBeEqualTo(404)
    }

    @Test
    fun `should create pedido and then delete it`() = testApplication {
        val jsonClient = createJsonClient()

        val responsePost = jsonClient.post("/pedidos") {
            setBody(PedidosData.createPedido)
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
            contentType(ContentType.Application.Json)
        }

        val pedidoCreated = responsePost.body<PedidoDto>()

        val responseDelete = jsonClient.delete("/pedidos/${pedidoCreated.id}") {
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        responsePost.status.shouldBe(HttpStatusCode.Created)
        responseDelete.status.shouldBe(HttpStatusCode.NoContent)
        pedidoCreated.iva.shouldBeEqualTo(PedidosData.createPedido.iva)
    }

    @Test
    fun `should not delete unknown pedido`() = testApplication {
        val jsonClient = createJsonClient()

        val responseDelete = jsonClient.delete("/pedidos/63ebb2569be16967bba54c3b") {
            headers { set("Authorization", "Bearer ${PedidosData.token}") }
        }

        responseDelete.status.shouldBe(HttpStatusCode.NotFound)
    }

}