package routing

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.litote.kmongo.newId
import pedidosApi.dto.*
import pedidosApi.models.Tarea
import java.util.*
import kotlin.test.assertEquals


private val json = Json { ignoreUnknownKeys = true }
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class PedidosRoutingTest {

    private val config = ApplicationConfig("application.conf")

    private val usuario = UsuarioDto(UUID.randomUUID().toString(), "Nombre", "correo@email.com")

    private val producto = ProductoDto(UUID.randomUUID().toString(),
        "NombreProd", "categoriaProd",5 ,"descrProd", 12.2, "")

    private val tarea = TareaDto(
        id = newId<Tarea>().toString(),
        productos = listOf(producto),
        empleado = UsuarioDto(UUID.randomUUID().toString(), "empleadoUsername", "emp@email.com"))

    private val pedido = CreatePedidoDto(
        //id = newId<Pedido>().toString(),
        usuario = usuario.username,
        productos = listOf(producto.toString())
    )

    //prueba doc ktor
    @Test
    @Order(1)
    fun testPedidosGet() = testApplication  {
        environment { config }
        val response = client.get("/pedidos")/*{
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf(""))
        }*/
        assertEquals(HttpStatusCode.OK, response.status)
        //assertEquals("", response.bodyAsText())
    }

    @Test
    @Order(2)
    fun testPedidoPost() = testApplication {
        environment { config }
        val client = createClient {
            install(ContentNegotiation){
                json()
            }
        }
        val response = client.post("/pedidos"){
            contentType(ContentType.Application.Json)
            setBody(pedido)
        }

        //assertEquals(HttpStatusCode.Created, response.status)
        val result = response.bodyAsText()

        var dto = json.decodeFromString<PedidoDto>(result)
        assertAll(
            // Devuelve Fake User en vez del usuario de pedido
            { assertEquals(pedido.usuario, dto.usuario.username)}
        )
    }

    @Test
    @Order(3)
    fun testPedidosPut() = testApplication {
        environment { config }
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        var response = client.post("/pedidos"){
            contentType(ContentType.Application.Json)
            setBody(pedido)
        }
        var dto = json.decodeFromString<PedidoDto>(response.bodyAsText())

        response = client.put("/pedidos/${dto.id}") {
            contentType(ContentType.Application.Json)
            setBody(pedido)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.bodyAsText()
        dto = json.decodeFromString(result)

        assertAll(
            {assertEquals(pedido.usuario, dto.usuario.username)}
        )
    }

    @Test
    @Order(5)
    fun testPedidoPutNotFound() = testApplication {

        environment { config }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val response = client.put("/${UUID.randomUUID()}") {
            contentType(ContentType.Application.Json)
            setBody(pedido)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    @Order(6)
    fun testPedidoDelete() = testApplication {
        environment { config }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        var response = client.post("/pedidos") {
            contentType(ContentType.Application.Json)
            setBody(pedido)
        }

        val dto = json.decodeFromString<PedidoDto>(response.bodyAsText())

        response = client.delete("/${dto.id}")

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    @Order(7)
    fun testDeleteNotFound() = testApplication {
        environment { config }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val response = client.delete("/${UUID.randomUUID()}")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}