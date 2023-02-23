package blanco.maldonado.mendoza.apiproductos.controller

import blanco.maldonado.mendoza.apiproductos.dto.ProductoCreateDto
import blanco.maldonado.mendoza.apiproductos.mapper.toDto
import blanco.maldonado.mendoza.apiproductos.model.Producto
import blanco.maldonado.mendoza.apiproductos.repositories.ProductoCachedRepositoryImpl
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus

@ExtendWith(MockKExtension::class)
@SpringBootTest
class ProductoControllerTest {

    @MockK
    private lateinit var repository: ProductoCachedRepositoryImpl

    @InjectMockKs
    lateinit var controller: ProductoController

    private final val producto = Producto(
        nombre = "Teclado",
        categoria = Producto.Categoria.PIEZA,
        stock = 3,
        description = "Teclado para ordenador de sobremesa",
        precio = 15.50,
        activo = true
    )

    val productoDto = producto.toDto()

    val productoCreateDto = ProductoCreateDto(
        nombre = "Teclado",
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

        Assertions.assertAll(
            { assertNotNull(result) },
            { assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(1, res.count())},
            {}
        )
    }

    @Test
    fun findProductById() {
    }

    @Test
    fun findProductByCategoria() {
    }

    @Test
    fun findProductByNombre() {
    }

    @Test
    fun resultNombreNulo() {
    }

    @Test
    fun resultCategoriaNula() {
    }

    @Test
    fun createProduct() {
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