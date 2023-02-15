package repositories

import arrow.core.Either
import com.mongodb.client.result.DeleteResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.div
import org.litote.kmongo.eq
import pedidosApi.dto.ProductoDto
import pedidosApi.dto.UsuarioDto
import pedidosApi.exceptions.PedidoError
import pedidosApi.models.Pedido
import pedidosApi.models.Tarea
import pedidosApi.repositories.PedidosRepository
import java.util.*

class PedidosRepositoryTest {
    private val collectionMock = mockk<CoroutineCollection<Pedido>>()

    @MockK
    private val pedidosRepository = PedidosRepository(collectionMock)


    private val userId = UUID.randomUUID().toString()
    private val usuario = UsuarioDto(userId, "Nombre", "correo@email.com")

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

    @Test
    fun save() = runBlocking {
        coEvery { collectionMock.save(pedido) } returns null
        val result: Either<PedidoError, Pedido> = pedidosRepository.save(pedido)
        result.getOrNull() shouldBeEqualTo pedido
        coVerify { collectionMock.save(pedido) }
    }

    @Test
    fun getByPage() = runBlocking {
        coEvery { collectionMock.find().skip(0).limit(1).toFlow() } returns flowOf(pedido)
        val result = pedidosRepository.getByPage(0, 1)
        result.getOrNull()?.first() shouldBeEqualTo pedido
        result.getOrNull()?.size shouldBeEqualTo 1
        result.getOrNull()?.page shouldBeEqualTo 0

        coVerify(exactly = 1) {
            collectionMock.find().skip(0).limit(1).toFlow()
        }
    }

    @Test
    fun getById(): Unit = runBlocking {
        coEvery { collectionMock.find(Pedido::_id eq pedido._id).first() } returns pedido
        val result = pedidosRepository.getById(pedido._id.toString())
        result.getOrNull() shouldBeEqualTo pedido
        coVerify { collectionMock.find(Pedido::_id eq pedido._id).first() }
    }

    @Test
    fun getByUserId(): Unit = runBlocking {
        coEvery {
            collectionMock.find(Pedido::usuario / UsuarioDto::id eq userId).skip(0).limit(10).toFlow()
        } returns flowOf(pedido)
        val result = pedidosRepository.getByUserId(userId, 0, 10)
        result.getOrNull()?.first() shouldBeEqualTo pedido
        result.getOrNull()?.size shouldBeEqualTo 10
        result.getOrNull()?.page shouldBeEqualTo 0

        coVerify { collectionMock.find(Pedido::usuario / UsuarioDto::id eq userId).skip(0).limit(10).toFlow() }
    }

    @Test
    fun delete(): Unit = runBlocking {
        coEvery { collectionMock.deleteOneById(pedido._id) } returns DeleteResult.acknowledged(1)
        val result = pedidosRepository.delete(pedido._id.toString())
        result.getOrNull() shouldBeEqualTo Unit
        coVerify { collectionMock.deleteOneById(pedido._id) }
    }
}