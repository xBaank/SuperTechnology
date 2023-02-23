package blanco.maldonado.mendoza.apiproductos.controller

import blanco.maldonado.mendoza.apiproductos.dto.ProductoCreateDto
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoBadRequestException
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoNotFoundException
import blanco.maldonado.mendoza.apiproductos.mapper.toDto
import blanco.maldonado.mendoza.apiproductos.model.Producto
import blanco.maldonado.mendoza.apiproductos.repositories.ProductoCachedRepositoryImpl
import io.mockk.MockKException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*

@ExtendWith(MockKExtension::class)
@SpringBootTest
class ProductoControllerTest {

    @MockK
    private lateinit var repository: ProductoCachedRepositoryImpl

    @InjectMockKs
    lateinit var controller: ProductoController

    private final val producto = Producto(
        uuid = "a765df90-e4aa-4306-b93e-20500accf8f7",
        nombre = "Teclado",
        categoria = Producto.Categoria.PIEZA,
        stock = 3,
        description = "Teclado para ordenador de sobremesa",
        precio = 15.50,
        activo = true
    )

    val productoDto = producto.toDto()

    val productoCreateDto = ProductoCreateDto(
        nombre = "Movil",
        categoria = Producto.Categoria.PIEZA.name,
        stock = 3,
        description = "Teclado para ordenador de sobremesa",
        precio = 15.50,
        activo = true.toString()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllProductos() = runTest {
        coEvery { repository.findAll() } returns flowOf(producto)
        val result = controller.findAllProductos()
        val res = result.body!!.toList()

        assertAll(
            { assertNotNull(result) },
            { assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(1, res.count()) },
            { assertEquals(productoDto.nombre, res[0].nombre) }
        )

        coVerify { repository.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findProductById() = runTest {
        coEvery { repository.findById(any()) } returns producto
        val result = controller.findProductById(producto.uuid!!)
        val res = result.body!!

        assertAll(
            { assertNotNull(result) },
            { assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(productoDto.precio, res.precio) },
            { assertEquals(productoDto.categoria, res.categoria) }
        )

        coVerify { repository.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByIdNotFound() = runTest {
        coEvery { repository.findById(any()) } throws ProductoNotFoundException("Producto no encontrado")
        val res = assertThrows<ResponseStatusException> {
            controller.findProductById(producto.uuid!!)
        }
        assertEquals(
            """404 NOT_FOUND "Producto no encontrado"""", res.message
        )
        coVerify { repository.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findProductByCategoria() = runTest {
        coEvery { repository.findByCategoria(any()) } returns flowOf(producto)
        val result = controller.findProductByCategoria(producto.categoria.name)
        val res = result.body!!.toList()

        assertAll(
            { assertNotNull(result) },
            { assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(1, res.count()) },
            { assertEquals(productoDto.stock, res[0].stock) }
        )
        coVerify { repository.findByCategoria(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findProductoByCategoriaNotFound() = runTest {
        coEvery { repository.findByCategoria(any()) } throws ProductoNotFoundException("Producto no encontrado")
        val res = assertThrows<ResponseStatusException> {
            controller.findProductByCategoria(producto.categoria.name)
        }
        assertEquals(
            """404 NOT_FOUND "Producto no encontrado"""", res.message
        )
        coVerify { repository.findByCategoria(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findProductByNombre() = runTest {
        coEvery { repository.findByNombre(any()) } returns flowOf(producto)
        val result = controller.findProductByNombre(producto.nombre)
        val res = result.body!!.toList()

        assertAll(
            { assertNotNull(result) },
            { assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(1, res.size) }
        )
        coVerify { repository.findByNombre(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findProductoByNombreNotFound() = runTest {
        coEvery { repository.findByNombre(any()) } throws ProductoNotFoundException("Producto no encontrado")
        val res = assertThrows<ResponseStatusException> {
            controller.findProductByNombre(producto.nombre)
        }
        assertEquals(
            """404 NOT_FOUND "Producto no encontrado"""", res.message
        )
        coVerify { repository.findByNombre(any()) }
    }

    @Test
    fun resultNombreNulo() {
    }

    @Test
    fun resultCategoriaNula() {
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createProduct() = runTest {
        coEvery { repository.save(any()) } returns producto
        coEvery { repository.findByNombre(any()) } returns emptyFlow()
        val result = controller.createProduct(productoCreateDto)
        val res = result.body!!

        assertAll(
            { assertNotNull(result) },
            { assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.CREATED) },
            { assertEquals(productoDto.description, res.description) }
        )
        coVerify { repository.save(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createProductoNombreBlanco() = runTest {
        coEvery { repository.save(any()) } throws ProductoBadRequestException("")
        val res = assertThrows<MockKException> {
            controller.createProduct(productoCreateDto.copy(nombre = " "))
        }
        assertEquals(
            """no answer found for: ProductoCachedRepositoryImpl(repository#1).findByNombre( , continuation {})""", res.message
        )
        coVerify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun updateProduct() {
    }

    @Test
    fun deleteProduct() {
    }

    @Test
    fun getAll() {
    }
}