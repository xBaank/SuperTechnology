package blanco.maldonado.mendoza.apiproductos.controller

import blanco.maldonado.mendoza.apiproductos.config.APIConfig
import blanco.maldonado.mendoza.apiproductos.dto.ProductoCreateDto
import blanco.maldonado.mendoza.apiproductos.dto.ProductoDTO
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoBadRequestExcepcion
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoConflictIntegrutyException
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoNotFoundException
import blanco.maldonado.mendoza.apiproductos.mapper.toDto
import blanco.maldonado.mendoza.apiproductos.mapper.toModel
import blanco.maldonado.mendoza.apiproductos.model.Producto
import blanco.maldonado.mendoza.apiproductos.model.TestDto
import blanco.maldonado.mendoza.apiproductos.repositories.ProductosRepository
import blanco.maldonado.mendoza.apiproductos.validator.validate
import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
    private val repository: ProductosRepository
) {
    /**
     * Get all : Lista de productos
     * @return ResponseEntity<Flow<ProductosDTO>>
     */
    @GetMapping("")
    suspend fun findAllProductos(): ResponseEntity<List<Producto>> = withContext(Dispatchers.IO) {
        logger.info { "Get productos" }
        val res = repository.findAll().toList()
        //val res = flowOf(ProductoDTO(null, "Pepe", Producto.Categoria.PIEZA, 3, "Prueba", 2.13, true))
        return@withContext ResponseEntity.ok(res)
    }

    /**
     * Get {id} : producto por su id
     * @param id : uid del producto
     * @return ResponseEntity<ProductosDTO>
     * @throws ResponseStatusException con el mensaje 404 si no lo encuentra
     */
    @GetMapping("/{id}")
    suspend fun findProductById(@PathVariable id: UUID): ResponseEntity<ProductoDTO> = withContext(Dispatchers.IO) {

        logger.info { "Obteniendo producto por id" }

        try {
            val res = repository.findById(id)?.toDto()
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
    @GetMapping("/{categoria}")
    suspend fun findProductByCategoria(@PathVariable categoria: String): ResponseEntity<Flow<ProductoDTO>> =
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
    @GetMapping("/{nombre}")
    suspend fun findProductById(@PathVariable nombre: String): ResponseEntity<ProductoDTO> =
        withContext(Dispatchers.IO) {
            logger.info { "Obteniendo producto por id" }

            try {
                val res = repository.findByNombre(nombre).toDto()
                return@withContext ResponseEntity.ok(res)
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Post : crea un producto
     * @param procuctoDTO : producto
     * @return ResponseEntity<ProductosDTO>
     * @throws ResponseStatusException con el mensaje 400 si no es valido
     */
    @PostMapping("")
    suspend fun createProduct(@Valid @RequestBody productoDto: ProductoCreateDto): ResponseEntity<ProductoDTO> =
        withContext(Dispatchers.IO) {
            //todo ver si el producto exixtepor id o por nombre, y si no exixte crearlo si existe no posible
            logger.info { "Creando un producto" }
            try {
                val p = productoDto.validate().toModel()
                val res = repository.save(p).toDto()
                return@withContext ResponseEntity.status(HttpStatus.CREATED).body(res)
            } catch (e: ProductoBadRequestExcepcion) {
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
    @PutMapping("/{id}")
    suspend fun updateProduct(
        @PathVariable id: UUID, @Valid @RequestBody productoDto: ProductoCreateDto
    ): ResponseEntity<ProductoDTO> = withContext(Dispatchers.IO) {
        //todo ver si el producto exixte, y si no exixte que la respuesta sea not found
        logger.info { "Modificando producto con id $id" }
        try {
            val p = productoDto.validate().toModel()
            val res = repository.findById(id)!!.toDto()
            res.let {
                repository.save(p).toDto()
            }
            return@withContext ResponseEntity.status(HttpStatus.OK).body(res)
        } catch (e: ProductoNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: ProductoBadRequestExcepcion) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    //path modificar o deshabilitar
    @DeleteMapping("/{id}")
    suspend fun deleteProduct(@PathVariable id: UUID): ResponseEntity<ProductoDTO> = withContext(Dispatchers.IO) {
        //todo ver si el producto exixte, y si no not posible
        logger.info { "Borrando producto" }
        try {
            repository.deleteById(id)
            return@withContext ResponseEntity.noContent().build()
        } catch (e: ProductoNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: ProductoConflictIntegrutyException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, e.message)
        }

    }

    @GetMapping("/test")
    fun getAll(@RequestParam texto: String?): ResponseEntity<List<TestDto>> {
        logger.info { "GET ALL Test" }
        return ResponseEntity.ok(listOf(TestDto("Hola : Query: $texto"), TestDto("Mundo : Query: $texto")))
    }


}