package repositories

import arrow.core.Either
import com.mongodb.reactivestreams.client.MongoDatabase
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.newId
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.reactivestreams.save
import pedidosApi.dto.CreatePedidoDto
import pedidosApi.dto.ProductoDto
import pedidosApi.dto.TareaDto
import pedidosApi.dto.UsuarioDto
import pedidosApi.exceptions.PedidoError
import pedidosApi.models.Pedido
import pedidosApi.models.Tarea
import pedidosApi.repositories.PedidosRepository
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@OptIn(ExperimentalCoroutinesApi::class)
class PedidosRepositoryTest {
    private val mongo = mockk<CoroutineDatabase>("pedidos")
    private val pedidosRepository = PedidosRepository(mongo)



    private val usuario = UsuarioDto(UUID.randomUUID().toString(), "Nombre", "correo@email.com")

    private val producto = ProductoDto(
        UUID.randomUUID().toString(),
        "NombreProd", "categoriaProd", 5, "descrProd", 12.2, ""
    )

    private val tarea = Tarea(
        productos = listOf(producto),
        empleado = UsuarioDto(UUID.randomUUID().toString(), "empleadoUsername", "emp@email.com"),
        createdAt = 12356L
    )

    private val pedido = Pedido(
        usuario = usuario,
        tareas = listOf(tarea),
        iva = 0.21,
        estado = "PENDIENTE",
        createdAt = 1234L
    )

    companion object{
        @JvmStatic
        @BeforeAll
        fun initialize() = runBlocking {
            val x = mockk<MongoDatabase>("pedidos").getCollection<Pedido>().drop()
        }
    }

    @BeforeEach
    fun data() = runBlocking {
        val x = mockk<MongoDatabase>("pedidos").getCollection<Pedido>().save(pedido)
    }

    @AfterAll
    fun tearDown() = runBlocking {
        val x = mockk<MongoDatabase>("pedidos").getCollection<Pedido>().drop()
    }

    @Test
    @Order(1)
    fun save() = runTest {
        var result: Either<PedidoError, Pedido>? = null
        launch { result = pedidosRepository.save(pedido) }

    }
}