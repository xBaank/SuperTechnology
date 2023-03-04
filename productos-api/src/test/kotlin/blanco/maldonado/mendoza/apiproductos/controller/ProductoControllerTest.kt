package blanco.maldonado.mendoza.apiproductos.controller

import blanco.maldonado.mendoza.apiproductos.dto.ProductoCreateDto
import blanco.maldonado.mendoza.apiproductos.mapper.toDto
import blanco.maldonado.mendoza.apiproductos.model.Producto
import blanco.maldonado.mendoza.apiproductos.repositories.ProductoCachedRepositoryImpl
import blanco.maldonado.mendoza.apiproductos.user.User
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

    private var producto = Producto(
        id = 0L,
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

    val superAdmin = User(
        username = "superadmin",
        email = "superadmin@admin.com",
        password = "super1234",
        role = User.UserRole.SUPER_ADMIN,
        active = true
    )
    val admin = User(
        username = "admin",
        email = "admin@admin.com",
        password = "admin1234",
        role = User.UserRole.ADMIN,
        active = true
    )

    val usuario = User(
        username = "usuario",
        email = "usuario@usuario.com",
        password = "usuario1234",
        role = User.UserRole.USER,
        active = true
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
        coEvery { repository.findById(any()) } returns null
        val res = assertThrows<ResponseStatusException> {
            controller.findProductById("id erroneo")
        }
        assertEquals(
            """404 NOT_FOUND "Producto no encontrado con id: id erroneo ."""", res.message
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
        coEvery {
            repository.findByCategoria("MOVIL")
        } returns flowOf()

        val res = assertThrows<ResponseStatusException> {
            controller.findProductByCategoria("MOVIL")
        }
        assertEquals(
            """404 NOT_FOUND "La categoria MOVIL es correcta pero no tiene productos asociados."""", res.message
        )
        coVerify { repository.findByCategoria(any()) }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findProductoByCategoriaIsEmty() = runTest {
        coEvery {
            repository.findByCategoria("  ")
        } returns flowOf()

        val res = assertThrows<ResponseStatusException> {
            controller.findProductByCategoria("  ")
        }
        assertEquals(
            """404 NOT_FOUND "La categoria esta vacia."""", res.message
        )
        //si la categoría es vacia no llama al metodo
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findProductoByCategoriaNotCorrect() = runTest {
        coEvery {
            repository.findByCategoria("MOVILES")
        } returns flowOf()

        val res = assertThrows<ResponseStatusException> {
            controller.findProductByCategoria("MOVILES")
        }
        assertEquals(
            """404 NOT_FOUND "La categoria MOVILES no es correcta."""", res.message
        )
        //si la categoría no es correcta no llama al metodo
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
        coEvery { repository.findByNombre("nombre erroneo") } returns flowOf()

        val res = assertThrows<ResponseStatusException> {
            controller.findProductByNombre("nombre erroneo")
        }
        assertEquals(
            """404 NOT_FOUND "No se ha encontrado ningún producto con el nombre nombre erroneo"""", res.message
        )
        coVerify { repository.findByNombre(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun resultNombreNulo() = runTest {
        val res = assertThrows<ResponseStatusException> {
            controller.resultNombreNulo()
        }

        assertEquals(
            """404 NOT_FOUND "El nombre que ha introducido es nulo."""", res.message
        )

        //si el nombre es nulo el metodo no llama a el repositorio
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun resultCategoriaNula() = runTest {
        val res = assertThrows<ResponseStatusException> {
            controller.resultCategoriaNula()
        }

        assertEquals(
            """404 NOT_FOUND "La categoria que ha introducido es nula."""", res.message
        )

        //si la categoria es nula el metodo no llama a el repositorio
    }


    //todo no va y no se porque
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createProduct() = runTest {
        coEvery { repository.save(any()) } returns producto
        coEvery { repository.findByNombre(any()) } returns emptyFlow()
        val result = controller.createProduct(superAdmin, productoCreateDto)
        val res = result.body!!

        assertAll(
            { assertNotNull(result) },
            { assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.CREATED) },
            { assertEquals(productoDto.description, res.description) }
        )
        coVerify { repository.save(any()) }
    }


//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun createProductoNombreBlanco() = runTest {
//        coEvery { repository.save(any()) } returns producto
//        coEvery { repository.findByNombre(any()) } returns emptyFlow()
//
//        val productoCreateDto1 = ProductoCreateDto(
//            nombre = "  ",
//            categoria = Producto.Categoria.PIEZA.name,
//            stock = 3,
//            description = "Teclado para ordenador de sobremesa",
//            precio = 15.50,
//            activo = true.toString()
//        )
//
//        val res = assertThrows<ResponseStatusException> {
//            val result = controller.createProduct(productoCreateDto1)
//        }
//
//        assertEquals(
//            """400 BAD_REQUEST "El nombre no puede estar vacío"""", res.message
//        )
//
//    }
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun createProductoCategoriaVacia() = runTest {
//
//        coEvery { repository.save(any()) } returns producto
//        coEvery { repository.findByNombre(any()) } returns emptyFlow()
//
//        val productoCreateDto2 = ProductoCreateDto(
//            nombre = "nombre ",
//            categoria = " ",
//            stock = 3,
//            description = "Teclado para ordenador de sobremesa",
//            precio = 15.50,
//            activo = true.toString()
//        )
//
//        val res = assertThrows<ResponseStatusException> {
//            val result = controller.createProduct(productoCreateDto2)
//        }
//
//        assertEquals(
//            """400 BAD_REQUEST "La categoría no puede estar vacía"""", res.message
//        )
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun createProductoCategoriaIncorrecta() = runTest {
//
//        coEvery { repository.save(any()) } returns producto
//        coEvery { repository.findByNombre(any()) } returns emptyFlow()
//
//        val productoCreateDto3 = ProductoCreateDto(
//            nombre = "nombre ",
//            categoria = "incorrecta",
//            stock = 3,
//            description = "Teclado para ordenador de sobremesa",
//            precio = 15.50,
//            activo = true.toString()
//        )
//
//        val res = assertThrows<ResponseStatusException> {
//            val result = controller.createProduct(productoCreateDto3)
//        }
//
//        assertEquals(
//            """400 BAD_REQUEST "La categoría no es una categoria correcta"""", res.message
//        )
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun createProductoStockNegativo() = runTest {
//
//        coEvery { repository.save(any()) } returns producto
//        coEvery { repository.findByNombre(any()) } returns emptyFlow()
//
//        val productoCreateDto4 = ProductoCreateDto(
//            nombre = "nombre ",
//            categoria = "MOVIL",
//            stock = -1,
//            description = "Teclado para ordenador de sobremesa",
//            precio = 15.50,
//            activo = true.toString()
//        )
//
//        val res = assertThrows<ResponseStatusException> {
//            val result = controller.createProduct(productoCreateDto4)
//        }
//
//        assertEquals(
//            """400 BAD_REQUEST "El stock no puede ser negativo"""", res.message
//        )
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun createProductoDescripcionVacia() = runTest {
//
//        coEvery { repository.save(any()) } returns producto
//        coEvery { repository.findByNombre(any()) } returns emptyFlow()
//
//        val productoCreateDto5 = ProductoCreateDto(
//            nombre = "nombre ",
//            categoria = "MOVIL",
//            stock = 2,
//            description = " ",
//            precio = 15.50,
//            activo = true.toString()
//        )
//
//        val res = assertThrows<ResponseStatusException> {
//            val result = controller.createProduct(productoCreateDto5)
//        }
//
//        assertEquals(
//            """400 BAD_REQUEST "La descripción no puede estar vacía"""", res.message
//        )
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun createProductoPrecioCero() = runTest {
//        coEvery { repository.save(any()) } returns producto
//        coEvery { repository.findByNombre(any()) } returns emptyFlow()
//
//        val productoCreateDto5 = ProductoCreateDto(
//            nombre = "nombre ",
//            categoria = "MOVIL",
//            stock = 2,
//            description = "descripcion ",
//            precio = 0.0,
//            activo = true.toString()
//        )
//
//        val res = assertThrows<ResponseStatusException> {
//            val result = controller.createProduct(productoCreateDto5)
//        }
//
//        assertEquals(
//            """400 BAD_REQUEST "El precio no puede ser cero o negativo"""", res.message
//        )
//
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun createProductoPrecioNegativo() = runTest {
//        coEvery { repository.save(any()) } returns producto
//        coEvery { repository.findByNombre(any()) } returns emptyFlow()
//
//        val productoCreateDto5 = ProductoCreateDto(
//            nombre = "nombre ",
//            categoria = "MOVIL",
//            stock = 2,
//            description = "descripcion",
//            precio = -1.00,
//            activo = true.toString()
//        )
//
//        val res = assertThrows<ResponseStatusException> {
//            val result = controller.createProduct(productoCreateDto5)
//        }
//
//        assertEquals(
//            """400 BAD_REQUEST "El precio no puede ser cero o negativo"""", res.message
//        )
//
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun createProductoActivoErroneo() = runTest {
//        coEvery { repository.save(any()) } returns producto
//        coEvery { repository.findByNombre(any()) } returns emptyFlow()
//
//        val productoCreateDto5 = ProductoCreateDto(
//            nombre = "nombre ",
//            categoria = "MOVIL",
//            stock = 2,
//            description = "descripcion",
//            precio = 15.50,
//            activo = "cosa"
//        )
//
//        val res = assertThrows<ResponseStatusException> {
//            val result = controller.createProduct(productoCreateDto5)
//        }
//
//        assertEquals(
//            """400 BAD_REQUEST "La característica activo solo puede ser true o false y estar en minúsculas"""", res.message
//        )
//
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun updateProduct()= runTest {
//
//        coEvery { repository.update(any(),any())
//        } returns producto
//
//        val result = controller.updateProduct("uuid", productoCreateDto)
//        val res = result.body!!
//
//        assertAll(
//            { assertNotNull(result) },
//            { assertNotNull(res) },
//            { assertEquals(result.statusCode, HttpStatus.OK) },
//
//        )
//        coVerify { repository.update(any(), any()) }
//
//
//
//
//    }
//
//
//
//
//
//
//
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun deleteProduct() = runTest {
//
//        coEvery { repository.delete("uuid")
//        } returns producto
//
//        val result = controller.deleteProduct("uuid")
//
//        assertAll(
//            { assertNotNull(result) },
//            { assertEquals(result.statusCode, HttpStatus.NO_CONTENT) },
//
//        )
//        coVerify { repository.delete("uuid") }
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun deleteProductNotFound() = runTest {
//
//        coEvery { repository.delete("uuid")
//        } returns null
//
//        val result = controller.deleteProduct("uuid")
//
//        assertAll(
//            { assertNotNull(result) },
//            { assertEquals(result.statusCode, HttpStatus.NO_CONTENT) },
//
//            )
//        coVerify { repository.delete("uuid") }
//
//    }
//

}