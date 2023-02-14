import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.*

class ApplicationTest {
    private val config = ApplicationConfig("application.conf")
    @Test
    fun trueIsTrue() {
        Assertions.assertTrue(true)
    }

    @Test
    fun testRoot() = testApplication {
        // Configuramos el entorno de test
        environment { config }

        // Lanzamos la consulta
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        //assertEquals("Hello, world!", response.bodyAsText())
    }
}