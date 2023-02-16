package blanco.maldonado.mendoza.apiproductos.controller

import blanco.maldonado.mendoza.apiproductos.config.APIConfig
import blanco.maldonado.mendoza.apiproductos.dto.ProductoCreateDto
import blanco.maldonado.mendoza.apiproductos.dto.ProductoDto
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoBadRequestException
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoConflictIntegrityException
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoNotFoundException
import blanco.maldonado.mendoza.apiproductos.mapper.toDto
import blanco.maldonado.mendoza.apiproductos.mapper.toModel
import blanco.maldonado.mendoza.apiproductos.model.Producto
import blanco.maldonado.mendoza.apiproductos.model.TestDto
import blanco.maldonado.mendoza.apiproductos.repositories.ProductoCachedRepositoryImpl
import blanco.maldonado.mendoza.apiproductos.validator.validate
import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

private val logger = KotlinLogging.logger { }

@RestController
@RequestMapping(APIConfig.API_PATH + "/productos")
class ProductoController
@Autowired constructor(
    private val repository: ProductoCachedRepositoryImpl
) {
    /**
     * Get all : Lista de productos
     * @return ResponseEntity<Flow<ProductosDTO>>
     */
    @GetMapping("/")
    suspend fun findAllProductos(): ResponseEntity<List<Producto>> = withContext(Dispatchers.IO) {
        logger.info { "Get productos" }
        val res = repository.findAll().toList()
        return@withContext ResponseEntity.ok(res)
    }

    /**
     * Get {id} : producto por su id
     * @param id : uid del producto
     * @return ResponseEntity<ProductosDTO>
     * @throws ResponseStatusException con el mensaje 404 si no lo encuentra
     */
    @GetMapping("/{id}")
    suspend fun findProductById(@PathVariable id: String): ResponseEntity<Flow<ProductoDto>> =
        withContext(Dispatchers.IO) {
            logger.info { "Obteniendo producto por id" }
            try {
                val res = repository.findByUUID(id).map { it.toDto() }
                return@withContext ResponseEntity.ok(res)
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Get {categoria} : productos por su categoria
     * @param categoria : categoria del producto
     * @return ResponseEntity<Flow<ProductosDTO>
     */
    @GetMapping("/categoria/{categoria}")
    suspend fun findProductByCategoria(@PathVariable categoria: String): ResponseEntity<Flow<ProductoDto>> =
        withContext(Dispatchers.IO) {
            logger.info { "Get productos by categoria" }
            val res = repository.findByCategoria(categoria).map { it.toDto() }
            return@withContext ResponseEntity.ok(res)
        }

    /**
     * Get {nombre} : producto por su nombre
     * @param nombre : nombre del producto
     * @return ResponseEntity<ProductosDTO>
     * @throws ResponseStatusException con el mensaje 404 si no lo encuentra
     */
    @GetMapping("/nombre/{nombre}")
    suspend fun findProductByNombre(@PathVariable nombre: String): ResponseEntity<Flow<ProductoDto>> =
        withContext(Dispatchers.IO) {
            logger.info { "Obteniendo producto por nombre" }
            try {
                val res = repository.findByNombre(nombre).map { it.toDto() }
                if (res.toList().isEmpty() || nombre.isEmpty()) {
                    throw ProductoNotFoundException("No se ha encontrado ningún producto con el nombre $nombre")
                }
                return@withContext ResponseEntity.ok(res)
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Post : crea un producto
     * @param procuctoDTO : producto
     * @return ResponseEntity<ProductosDTO>
     * @throws ResponseStatusException con el mensaje 400 si no es válido
     */
    @PostMapping("/post")
    suspend fun createProduct(@Valid @RequestBody productoDto: ProductoCreateDto): ResponseEntity<ProductoDto> {
        logger.info { "Creando un producto" }
        checkProducto(productoDto)
        try {
            val p = productoDto.validate().toModel()
            val res = repository.save(p).toDto()
            return ResponseEntity.status(HttpStatus.CREATED).body(res)
        } catch (e: ProductoBadRequestException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    /**
     * Put : modifica un producto
     * @param id : id producto a modificar
     * @param producto dto : informacion del prodcuto
     * @return ResponseEntity<ProductosDTO>
     * @throws ResponseStatusException con el mensaje 400 si no es válido
     */
    @PutMapping("/update/{id}")
    suspend fun updateProduct(
        @PathVariable id: String, @Valid @RequestBody productoDto: ProductoCreateDto
    ): ResponseEntity<ProductoDto> = withContext(Dispatchers.IO) {
        //todo ver si el producto exixte, y si no exixte que la respuesta sea not found
        logger.info { "Modificando producto con id $id" }
        try {
            val p = productoDto.validate().toModel()
            val res = repository.findByUUID(id).map { it.toDto() }.firstOrNull()
            res.let {
                repository.save(p).toDto()
            }
            return@withContext ResponseEntity.status(HttpStatus.OK).body(res)
        } catch (e: ProductoNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: ProductoBadRequestException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    //path modificar o deshabilitar
    @DeleteMapping("/delete/{id}")
    suspend fun deleteProduct(@PathVariable id: String): ResponseEntity<ProductoDto> = withContext(Dispatchers.IO) {
        //todo ver si el producto exixte, y si no not posible
        logger.info { "Borrando producto" }
        try {
            repository.delete(id)
            return@withContext ResponseEntity.noContent().build()
        } catch (e: ProductoNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: ProductoConflictIntegrityException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, e.message)
        }

    }

    private suspend fun checkProducto(producto: ProductoCreateDto) {
        val existe = repository.findByNombre(producto.nombre)
        if (existe.toList().isNotEmpty()) {
            throw ProductoBadRequestException("El producto con nombre ${producto.nombre} ya existe")
        }
    }

    @GetMapping("/test")
    fun getAll(@RequestParam texto: String?): ResponseEntity<List<TestDto>> {
        logger.info { "GET ALL Test" }
        return ResponseEntity.ok(listOf(TestDto("Hola : Query: $texto"), TestDto("Mundo : Query: $texto")))
    }


}