/**
 * @since 16/02/2023
 * @author Azahara Blanco, Alfredo Maldonado, Sebastian Mendoza
 */
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
import blanco.maldonado.mendoza.apiproductos.user.User
import blanco.maldonado.mendoza.apiproductos.validator.validate
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
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
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

private val logger = KotlinLogging.logger { }

/**
 * @author Azahara Blanco, Sebastian Mendoza y Alfredo Maldonado
 * @since 20/02/2023
 *
 * Producto controller: api endpoint controller.
 *
 * @property repository
 * @constructor Create empty Producto controller.
 */
@RestController
@RequestMapping(APIConfig.API_PATH + "/productos")
class ProductoController
@Autowired constructor(
    private val repository: ProductoCachedRepositoryImpl
) {
    /**
     * Find all productos: Find all the products.
     *
     * @return Return all the products.
     */
    @Operation(summary = "Get all productos", description = "Get the products list", tags = ["Products"])
    @ApiResponse(responseCode = "200", description = "Lista de Producto")
    @GetMapping("")
    suspend fun findAllProductos(@AuthenticationPrincipal u : User): ResponseEntity<Flow<ProductoDto>> = withContext(Dispatchers.IO) {
        logger.info { "Get productos" }
        val res = repository.findAll().map { it.toDto() }
        return@withContext ResponseEntity.ok(res)
    }


    /**
     * Find product by id: Find the product if the id of the product is exists.
     *
     * @param id
     * @return The product.
     */
    @Operation(summary = "Get Producto by ID", description = "Get product by ID", tags = ["Product"])
    @Parameter(name = "id", description = "ID del Producto", required = true, example = "1")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found with this id.")
    @ApiResponse(responseCode = "403", description = "You don`t have permission with this id.")
    @ApiResponse(responseCode = "401", description = "You are not authorized with this id.")
    @ApiResponse(responseCode = "500", description = "Intern error with this id.")
    @ApiResponse(responseCode = "400", description = "Incorrect petition with this id.")
    @GetMapping("/{id}")
    suspend fun findProductById(@AuthenticationPrincipal u : User ,@PathVariable id: String): ResponseEntity<ProductoDto> =
        withContext(Dispatchers.IO) {
            logger.info { "Obteniendo producto por id" }
            try {
                val res = repository.findById(id)?.toDto() ?: throw ProductoNotFoundException("Producto no encontrado con id: $id .")
                return@withContext ResponseEntity.ok(res)
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Find product by categoria: Find one product by category if the id of category is exists.
     *
     * @param categoria
     * @return Return the product.
     */
    @Operation(summary = "Get Producto by Category", description = "Modifica un objeto Producto", tags = ["Producto"])
    @Parameter(name = "category", description = "ID de la categoría", required = true, example = "1")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found with this id.")
    @ApiResponse(responseCode = "403", description = "You don`t have permission with this id.")
    @ApiResponse(responseCode = "401", description = "You are not authorized with this id.")
    @ApiResponse(responseCode = "500", description = "Intern error with this id.")
    @ApiResponse(responseCode = "400", description = "Incorrect petition with this id.")
    @GetMapping("/categoria/{categoria}")
    suspend fun findProductByCategoria(@AuthenticationPrincipal u : User,@PathVariable categoria: String): ResponseEntity<Flow<ProductoDto>> =
        withContext(Dispatchers.IO) {
            logger.info { "Get productos by categoria" }
            try {
                if (!categoria.trim().isEmpty()) {
                    try {
                        Producto.Categoria.valueOf(categoria.trim())
                        val res = repository.findByCategoria(categoria.trim()).map { it.toDto() }
                        if (!res.toList().isEmpty()) {
                            return@withContext ResponseEntity.ok(res)
                        } else {
                            throw ProductoNotFoundException("La categoria $categoria es correcta pero no tiene productos asociados.")
                        }
                    } catch (e: IllegalArgumentException) {
                        throw ProductoNotFoundException("La categoria $categoria no es correcta.")
                    }
                } else {
                    throw ProductoNotFoundException("La categoria esta vacia.")
                }
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }


    /**
     * Find product by nombre: Find the product by name if the name is exists in the database.
     *
     * @param nombre
     * @return The product.
     */
    @Operation(
        summary = "Get Producto by Nombre",
        description = "Get the product by name",
        tags = ["Producto"]
    )
    @Parameter(name = "name", description = "ID of name", required = true, example = "1")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found with this id.")
    @ApiResponse(responseCode = "403", description = "You don`t have permission with this id.")
    @ApiResponse(responseCode = "401", description = "You are not authorized with this id.")
    @ApiResponse(responseCode = "500", description = "Intern error with this id.")
    @ApiResponse(responseCode = "400", description = "Incorrect petition with this id.")
    @GetMapping("/nombre/{nombre}")
    suspend fun findProductByNombre(@AuthenticationPrincipal u : User, @PathVariable nombre: String): ResponseEntity<Flow<ProductoDto>> =
        withContext(Dispatchers.IO) {
            logger.info { "Obteniendo producto por nombre" }
            try {
                val res = repository.findByNombre(nombre.trim()).map { it.toDto() }
                if (res.toList().isEmpty() || nombre.trim().isEmpty()) {
                    throw ProductoNotFoundException("No se ha encontrado ningún producto con el nombre $nombre")
                }
                return@withContext ResponseEntity.ok(res)
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }


    /**
     * Result nombre nulo: Function tht throw 404 exception when name is null.
     *
     * @return Return exception 404 because product is not exists.
     */
    @Operation(
        summary = "Name is null",
        description = "Throw exception because the name of the product is null.",
        tags = ["Producto"]
    )
    @ApiResponse(responseCode = "404", description = "Product not found because the name is null")
    @GetMapping("/nombre/")
    suspend fun resultNombreNulo(@AuthenticationPrincipal u : User): ResponseEntity<Flow<ProductoDto>> =
        withContext(Dispatchers.IO) {
            logger.info { "Obteniendo producto por nombre nulo" }
            try {
                throw ProductoNotFoundException("El nombre que ha introducido es nulo.")
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Result categoria nula: Function that throw 404 exception when the category is null.
     *
     * @return Return the exception 404 because the category is not exists.
     */
    @Operation(
        summary = "Categoria is null",
        description = "Throw exception because the category of the product is not exists.",
        tags = ["Producto"]
    )
    @ApiResponse(responseCode = "404", description = "Producto not found because the category was null")
    @GetMapping("/categoria/")
    suspend fun resultCategoriaNula(@AuthenticationPrincipal u : User): ResponseEntity<Flow<ProductoDto>> =
        withContext(Dispatchers.IO) {
            logger.info { "Obteniendo producto por categoria nula" }
            try {
                throw ProductoNotFoundException("La categoria que ha introducido es nula.")
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Create product: Create the product if the product is no exists in the database with code 201.
     *
     * @param productoDto
     * @return Return the product that was created with code 201.
     */
    @Operation(summary = "Create Product", description = "Create the product object", tags = ["Producto"])
    @ApiResponse(responseCode = "201", description = "Product created")
    @PostMapping("")
    suspend fun createProduct(@AuthenticationPrincipal u : User,@Valid @RequestBody productoDto: ProductoCreateDto): ResponseEntity<ProductoDto> {
        logger.info { "Creando un producto" }
        productoDto.activo.lowercase()
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
     * Update product: Modifica un producto si el id del producto existe en la base de datos, con código 200.
     *
     * @param id
     * @param productoDto
     * @return Devuelve el producto modificado.
     */
    @Operation(summary = "Update Product", description = "update the product object", tags = ["Product"])
    @Parameter(name = "id", description = "Product ID", required = true, example = "1")
    @ApiResponse(responseCode = "200", description = "Product modified")
    @ApiResponse(responseCode = "404", description = "Product not found with this id.")
    @PutMapping("/{id}")
    suspend fun updateProduct(@AuthenticationPrincipal u : User,
        @PathVariable id: String, @Valid @RequestBody productoDto: ProductoCreateDto
    ): ResponseEntity<ProductoDto> = withContext(Dispatchers.IO) {
        logger.info { "Modificando producto con id $id" }
        try {
            val p = productoDto.validate().toModel()
            val res = repository.update(id, p)!!.toDto()
            return@withContext ResponseEntity.status(HttpStatus.OK).body(res)
        } catch (e: ProductoNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: ProductoBadRequestException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    /**
     * Delete product: Delete the product if the product id is existe in the database.
     *
     * @param id
     * @return Return the deleted product.
     */
    @Operation(summary = "Delete Product", description = "Delete the product object.", tags = ["Product"])
    @Parameter(name = "id", description = "ID of Product", required = true, example = "1")
    @ApiResponse(responseCode = "204", description = "Product deleted.")
    @ApiResponse(responseCode = "404", description = "Error to delete product with this id.")
    // todo @PreAuthorize("hasRole('SUPER_ADMIN')")
    //todo @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    suspend fun deleteProduct(@AuthenticationPrincipal u : User,  @PathVariable id: String): ResponseEntity<ProductoDto> = withContext(Dispatchers.IO) {
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

    /**
     * Check producto: Function that check if the name of product is exists in the database.
     *
     * @param producto
     */
    suspend fun checkProducto(producto: ProductoCreateDto) {
        val existe = repository.findByNombre(producto.nombre)
        if (existe.toList().isNotEmpty()) {
            throw ProductoBadRequestException("El producto con nombre ${producto.nombre} ya existe")
        }
    }

    /**
     * Get all:
     *
     * @param texto
     * @return
     */
    @GetMapping("/test")
    fun getAll(@RequestParam texto: String?): ResponseEntity<List<TestDto>> {
        logger.info { "GET ALL Test" }
        return ResponseEntity.ok(listOf(TestDto("Hola : Query: $texto"), TestDto("Mundo : Query: $texto")))
    }


}