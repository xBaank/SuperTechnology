package blanco.maldonado.mendoza.apiproductos.repositories

import blanco.maldonado.mendoza.apiproductos.model.Producto
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import java.lang.Boolean.TRUE

@ExtendWith(MockKExtension::class)
@SpringBootTest
class ProductoCachedRepositoryImplTest {
    private val producto = Producto(
        uuid = "91e0c247-c611-4ed2-8db8-a495f1f16fee",
        nombre = "ordenador",
        categoria = Producto.Categoria.PIEZA,
        stock = 40,
        description = "prueba",
        precio = 43.44,
        activo = TRUE
    )

    @MockK
    lateinit var repo: ProductosRepository

    @InjectMockKs
    lateinit var repository: ProductoCachedRepositoryImpl

    init {
        MockKAnnotations.init(this)
    }

    @Test
    fun findAll() = runTest {
        coEvery {
            repo.findAll()
        } returns flowOf(producto)

        val result = repo.findAll().toList()

        assertAll(
            { assertEquals(1, result.size) },
            { assertEquals(producto, result[0]) }
        )
        coVerify(exactly = 1) { repo.findAll() }
    }

    @Test
    fun findById() = runTest {
        coEvery { repo.findByUuid(any()) } returns flowOf(producto)
        val result = repository.findById(producto.uuid!!)!!

        assertAll(
            { assertNotNull(result) },
            { assertEquals(producto.nombre, result.nombre) },
            { assertEquals(producto.categoria, result.categoria) },
            { assertEquals(producto.stock, result.stock) },
            { assertEquals(producto.description, result.description) },
            { assertEquals(producto.precio, result.precio) },
            { assertEquals(producto.activo, result.activo) },
        )
        coVerify { repo.findByUuid(any()) }
    }

    @Test
    fun findByIdNotFound() = runTest {
        coEvery { repo.findByUuid(any()) } returns emptyFlow()

        val result = repository.findById(producto.uuid!!)

        assertNull(result)

        coVerify { repo.findByUuid(any()) }
    }

    @Test
    fun findByCategoria() = runTest {
        coEvery { repo.findByCategoria(any()) } returns flowOf(producto)

        val result = repository.findByCategoria("PIEZA").toList()

        assertAll(
            { assertEquals(1, result.size) },
            { assertEquals(producto, result[0]) }
        )
        coVerify { repo.findByCategoria(any()) }
    }

    @Test
    fun finByCategoriaNotFound() = runTest {
        coEvery { repo.findByCategoria(any()) } returns flowOf()

        val result = repository.findByCategoria("Test").toList()

        assertAll(
            { assertEquals(0, result.size) }
        )
    }

    @Test
    fun findByNombre() = runTest {
        coEvery { repo.findByNombre(any()) } returns flowOf(producto)

        val result = repository.findByNombre("ordenador").toList()

        assertAll(
            { assertEquals(1, result.size) },
            { assertEquals(producto, result[0]) }
        )
        coVerify { repo.findByNombre(any()) }

    }

    @Test
    fun findByNombreNotFound() = runTest {
        coEvery { repo.findByNombre(any()) } returns flowOf()

        val result = repository.findByNombre("Test").toList()

        assertAll(
            { assertEquals(0, result.size) }
        )
    }

    @Test
    fun save() = runTest {
        coEvery { repo.save(any()) } returns producto

        val result = repository.save(producto)

        assertAll(
            { assertEquals(producto.nombre, result.nombre) },
            { assertEquals(producto.categoria, result.categoria) },
            { assertEquals(producto.stock, result.stock) },
            { assertEquals(producto.description, result.description) },
            { assertEquals(producto.precio, result.precio) },
            { assertEquals(producto.activo, result.activo) },
        )
        coVerify { repo.save(any()) }
    }

    @Test
    fun update() = runTest {
        coEvery { repo.findByUuid(any()) } returns flowOf(producto)
        coEvery { repo.save(any()) } returns producto

        val result = repository.update(producto.nombre, producto)!!

        assertAll(
            { assertEquals(producto.nombre, result.nombre) },
            { assertEquals(producto.categoria, result.categoria) },
            { assertEquals(producto.stock, result.stock) },
            { assertEquals(producto.description, result.description) },
            { assertEquals(producto.precio, result.precio) },
            { assertEquals(producto.activo, result.activo) },
        )
        coVerify { repo.save(any()) }
    }

    @Test
    fun updateNotFound() = runTest {
        coEvery { repo.findByUuid(any()) } returns flowOf()

        val result = repository.update(producto.nombre, producto)

        assertNull(result)
    }

    @Test
    fun delete() = runTest {
        coEvery { repo.findByUuid(any()) } returns flowOf(producto)
        coEvery { repo.delete(any()) } returns Unit
        repository.delete(producto.uuid!!)
        coVerify { repo.findByUuid(any()) }
    }
}