/**
 * @since 16/02/2023
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
package blanco.maldonado.mendoza.apiproductos.controller

import blanco.maldonado.mendoza.apiproductos.config.APIConfig
import blanco.maldonado.mendoza.apiproductos.dto.*
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoBadRequestException
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoConflictIntegrityException
import blanco.maldonado.mendoza.apiproductos.exceptions.ProductoNotFoundException
import blanco.maldonado.mendoza.apiproductos.exceptions.UserExceptionBadRequest
import blanco.maldonado.mendoza.apiproductos.mapper.toDto
import blanco.maldonado.mendoza.apiproductos.mapper.toDtoUser
import blanco.maldonado.mendoza.apiproductos.mapper.toModel
import blanco.maldonado.mendoza.apiproductos.model.Producto
import blanco.maldonado.mendoza.apiproductos.repositories.ProductoCachedRepositoryImpl
import blanco.maldonado.mendoza.apiproductos.user.User
import blanco.maldonado.mendoza.apiproductos.validator.validate
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

private val logger = KotlinLogging.logger { }

/**
 * Producto controller: api endpoint controller.
 *
 * @property repository
 * @constructor Implementation of cached repository
 */
@RestController
@RequestMapping(APIConfig.API_PATH + "/products")
class ProductoController
@Autowired constructor(
    private val repository: ProductoCachedRepositoryImpl
) {
    /**
     * Find all products: Get all active products from database.
     *
     * @return Return all active products.
     */
    @Operation(
        summary = "Get all products",
        description = "Get a list of all products from database",
        tags = ["All users"]
    )
    @ApiResponse(responseCode = "200", description = "List of products")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("")
    suspend fun findAllProductsUsers(): ResponseEntity<Flow<ProductoDtoUser>> =
        withContext(Dispatchers.IO) {
            logger.info { "Get all products" }
            try {
                val res = repository.findAll().filter { it.activo }.map { it.toDtoUser() }
                return@withContext ResponseEntity.ok(res)
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Find all products - Admins: Get all products from database with details.
     *
     * @return Return all products with details .
     */
    @Operation(
        summary = "Get all products by admins",
        description = "Get a list of all products from database by admins",
        tags = ["All admins"]
    )
    @ApiResponse(responseCode = "200", description = "List of products")
    @ApiResponse(responseCode = "401", description = "You are not authorized with this user.")
    @ApiResponse(responseCode = "403", description = "You don`t have permission with this user.")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/admin")
    suspend fun findAllProductsAdmins(@AuthenticationPrincipal u: User): ResponseEntity<Flow<ProductoDto>> =
        withContext(Dispatchers.IO) {
            logger.info { "Get all products" }
            try {
                val res = repository.findAll().map { it.toDto() }
                return@withContext ResponseEntity.ok(res)
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
            }
        }

    /**
     * Find product by id: Find the product active if the id of the product exists.
     *
     * @param id
     * @return The product.
     */
    @Operation(summary = "Get product by ID", description = "Get a single product by ID", tags = ["All users"])
    @Parameter(
        name = "id",
        description = "ID of the product",
        required = true,
        example = "71ae8148-4407-4f3e-8bb6-2de256e8783e"
    )
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "400", description = "Incorrect petition with this id.")
    @ApiResponse(responseCode = "404", description = "Product not found with this id.")
    @ApiResponse(responseCode = "500", description = "Intern error with this id.")
    @GetMapping("/{id}")
    suspend fun findProductByIdUsers(
        @PathVariable id: String
    ): ResponseEntity<ProductoDtoUser> =
        withContext(Dispatchers.IO) {
            logger.info { "Get product by ID" }
            try {
                val res = repository.findById(id).takeIf { it?.activo ?: true }?.toDtoUser()
                    ?: throw ProductoNotFoundException("Product not found with this id: $id.")
                return@withContext ResponseEntity.ok(res)
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Find product by id - Admins: Find the product if the id of the product exists with details.
     *
     * @param id
     * @return The product.
     */
    @Operation(
        summary = "Get product by ID - Admins",
        description = "Get a single product by ID with all details",
        tags = ["All admins"]
    )
    @Parameter(
        name = "id",
        description = "ID of the product",
        required = true,
        example = "71ae8148-4407-4f3e-8bb6-2de256e8783e"
    )
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "400", description = "Incorrect petition with this id.")
    @ApiResponse(responseCode = "401", description = "You are not authorized with this user.")
    @ApiResponse(responseCode = "403", description = "You don`t have permission with this user.")
    @ApiResponse(responseCode = "404", description = "Product not found with this id.")
    @ApiResponse(responseCode = "500", description = "Intern error with this id.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/admin/{id}")
    suspend fun findProductByIdAdmins(
        @AuthenticationPrincipal u: User,
        @PathVariable id: String
    ): ResponseEntity<ProductoDto> =
        withContext(Dispatchers.IO) {
            logger.info { "Get product by ID" }
            try {
                val res = repository.findById(id)?.toDto()
                    ?: throw ProductoNotFoundException("Product not found with this id: $id.")
                return@withContext ResponseEntity.ok(res)
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Find product by category: Find a single active product by category if the id of category exists.
     *
     * @param categoria
     * @return Return the product.
     */
    @Operation(
        summary = "Get Producto by Category",
        description = "Get a single product by category name",
        tags = ["All users"]
    )
    @Parameter(name = "category", description = "name of the category", required = true, example = "PIEZA")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "400", description = "Incorrect petition with this name.")
    @ApiResponse(responseCode = "404", description = "Product not found with this name.")
    @ApiResponse(responseCode = "500", description = "Intern error with this id.")
    @GetMapping("/category/{categoria}")
    suspend fun findProductByCategoriaUsers(
        @PathVariable categoria: String
    ): ResponseEntity<Flow<ProductoDtoUser>> =
        withContext(Dispatchers.IO) {
            logger.info { "Get products by category name" }
            try {
                if (!categoria.trim().uppercase().isEmpty()) {
                    try {
                        Producto.Categoria.valueOf(categoria.trim().uppercase())
                        val res =
                            repository.findByCategoria(categoria.trim().uppercase()).filter { it.activo }
                                .map { it.toDtoUser() }
                        if (!res.toList().isEmpty()) {
                            return@withContext ResponseEntity.ok(res)
                        } else {
                            throw ProductoNotFoundException("The $categoria category is correct but there are no products.")
                        }
                    } catch (e: IllegalArgumentException) {
                        throw ProductoNotFoundException("The $categoria category is not correct.")
                    }
                } else {
                    throw ProductoNotFoundException("Category is empty.")
                }
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Find product by category - Admins: Find one product by category if the id of category exists with details.
     *
     * @param categoria
     * @return Return the product.
     */
    @Operation(
        summary = "Get Producto by Category - Admins",
        description = "Get a single product by category name",
        tags = ["All admins"]
    )
    @Parameter(name = "category", description = "name of the category", required = true, example = "PIEZA")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "400", description = "Incorrect petition with this name.")
    @ApiResponse(responseCode = "401", description = "You are not authorized with this user.")
    @ApiResponse(responseCode = "403", description = "You don`t have permission with this user.")
    @ApiResponse(responseCode = "404", description = "Product not found with this name.")
    @ApiResponse(responseCode = "500", description = "Intern error with this id.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/admin/category/{categoria}")
    suspend fun findProductByCategoriaAdmins(
        @AuthenticationPrincipal u: User,
        @PathVariable categoria: String
    ): ResponseEntity<Flow<ProductoDto>> =
        withContext(Dispatchers.IO) {
            logger.info { "Get products by categoria" }
            try {
                if (!categoria.trim().isEmpty()) {
                    try {
                        Producto.Categoria.valueOf(categoria.trim())
                        val res = repository.findByCategoria(categoria.trim()).map { it.toDto() }
                        if (!res.toList().isEmpty()) {
                            return@withContext ResponseEntity.ok(res)
                        } else {
                            throw ProductoNotFoundException("The $categoria category is correct but there are no products.")
                        }
                    } catch (e: IllegalArgumentException) {
                        throw ProductoNotFoundException("The $categoria category is not correct.")
                    }
                } else {
                    throw ProductoNotFoundException("Category is empty.")
                }
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }


    /**
     * Find product by name: Find a active single product by name if the name exists.
     *
     * @param nombre
     * @return The product.
     */
    @Operation(
        summary = "Get Producto by name",
        description = "Get the product by name",
        tags = ["All users"]
    )
    @Parameter(name = "name", description = "ID of name", required = true, example = "teclado")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "400", description = "Incorrect petition with this name.")
    @ApiResponse(responseCode = "404", description = "Product not found with this name.")
    @ApiResponse(responseCode = "500", description = "Intern error with this name.")
    @GetMapping("/name/{nombre}")
    suspend fun findProductByNombreUsers(
        @PathVariable nombre: String
    ): ResponseEntity<Flow<ProductoDtoUser>> =
        withContext(Dispatchers.IO) {
            logger.info { "Get the product by name" }
            try {
                val res = repository.findByNombre(nombre.trim()).filter { it.activo }.map { it.toDtoUser() }
                if (res.toList().isEmpty() || nombre.trim().isEmpty()) {
                    throw ProductoNotFoundException("No product found with the name $nombre")
                }
                return@withContext ResponseEntity.ok(res)
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Find product by name - Admins: Find the product by name if the name exists in the database with details.
     *
     * @param nombre
     * @return The product.
     */
    @Operation(
        summary = "Get Producto by name - Admins",
        description = "Get the product by name",
        tags = ["All admins"]
    )
    @Parameter(name = "name", description = "ID of name", required = true, example = "PIEZA")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "400", description = "Incorrect petition with this name.")
    @ApiResponse(responseCode = "401", description = "You are not authorized with this name.")
    @ApiResponse(responseCode = "403", description = "You don`t have permission with this name.")
    @ApiResponse(responseCode = "404", description = "Product not found with this name.")
    @ApiResponse(responseCode = "500", description = "Intern error with this name.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/admin/name/{nombre}")
    suspend fun findProductByNombreAdmins(
        @AuthenticationPrincipal u: User,
        @PathVariable nombre: String
    ): ResponseEntity<Flow<ProductoDto>> =
        withContext(Dispatchers.IO) {
            logger.info { "Get product by name" }
            try {
                val res = repository.findByNombre(nombre.trim()).map { it.toDto() }
                if (res.toList().isEmpty() || nombre.trim().isEmpty()) {
                    throw ProductoNotFoundException("No product found with the name $nombre")
                }
                return@withContext ResponseEntity.ok(res)
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }


    /**
     * Result name null: Function that throw 404 exception when name is null.
     *
     * @return Return exception 404 because product is not exists.
     */
    @Operation(
        summary = "Name is null",
        description = "Throw exception because the name of the product is null.",
        tags = ["All users"]
    )
    @ApiResponse(responseCode = "404", description = "Product not found because the name is null")
    @GetMapping("/name/")
    suspend fun resultNombreNuloUsers(): ResponseEntity<Flow<ProductoDtoUser>> =
        withContext(Dispatchers.IO) {
            logger.info { "Product with null name" }
            try {
                throw ProductoNotFoundException("The name you entered is null.")
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Result name null: Function that throw 404 exception when name is null.
     *
     * @return Return exception 404 because product is not exists.
     */
    @Operation(
        summary = "Name is null - Admins",
        description = "Throw exception because the name of the product is null.",
        tags = ["All admins"]
    )
    @ApiResponse(responseCode = "403", description = "You don`t have permission with this user.")
    @ApiResponse(responseCode = "404", description = "Product not found because the name is null")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/admin/name/")
    suspend fun resultNombreNuloAdmin(@AuthenticationPrincipal u: User): ResponseEntity<Flow<ProductoDto>> =
        withContext(Dispatchers.IO) {
            logger.info { "Product with null name" }
            try {
                throw ProductoNotFoundException("The name you entered is null.")
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
        summary = "Category is null",
        description = "Throw exception because the category of the product is not exists.",
        tags = ["All users"]
    )
    @ApiResponse(responseCode = "404", description = "Producto not found because the category was null")
    @GetMapping("/category/")
    suspend fun resultCategoriaNulaUsers(): ResponseEntity<Flow<ProductoDtoUser>> =
        withContext(Dispatchers.IO) {
            logger.info { "Product by null category" }
            try {
                throw ProductoNotFoundException("The category you entered is null.")
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Result category null: Function that throw 404 exception when the category is null.
     *
     * @return Return the exception 404 because the category is not exists.
     */
    @Operation(
        summary = "Category is null - Admins",
        description = "Throw exception because the category of the product is not exists.",
        tags = ["All admins"]
    )
    @ApiResponse(responseCode = "403", description = "You don`t have permission with this user.")
    @ApiResponse(responseCode = "404", description = "Producto not found because the category was null")
    @GetMapping("/admin/category/")
    suspend fun resultCategoriaNulaAdmin(): ResponseEntity<Flow<ProductoDto>> =
        withContext(Dispatchers.IO) {
            logger.info { "Product by null category" }
            try {
                throw ProductoNotFoundException("The category you entered is null.")
            } catch (e: ProductoNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            }
        }

    /**
     * Create product: Create the product if the product is no exists in the database.
     *
     * @param productoDto
     * @return Return the product that was created.
     */
    @Operation(summary = "Create Product", description = "Create the product object", tags = ["Super admin"])
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "401", description = "Forbidden because you don't have permission with this account.")
    @ApiResponse(responseCode = "403", description = "You don`t have permission with this user.")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @PostMapping("")
    suspend fun createProduct(
        @AuthenticationPrincipal u: User,
        @Valid @RequestBody productoDto: ProductoCreateDto
    ): ResponseEntity<ProductoDto> {
        logger.info { "Creando un producto" }
        productoDto.activo.lowercase()
        checkProducto(productoDto)
        try {
            val p = productoDto.validate().toModel()
            val res = repository.save(p).toDto()
            return ResponseEntity.status(HttpStatus.CREATED).body(res)
        } catch (e: ProductoBadRequestException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (e: UserExceptionBadRequest) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, e.message)
        }
    }

    /**
     * Update product: Modified the product if the id of product is exists in the database with code 200.
     *
     * @param id
     * @param productoDto
     * @return Return the modified product with code 200.
     */
    @Operation(summary = "Update Product", description = "update the product object", tags = ["All admins"])
    @Parameter(
        name = "id",
        description = "Product ID",
        required = true,
        example = "035d9aa1-e4d2-4e4d-b6ef-49b148ba1484"
    )
    @ApiResponse(responseCode = "200", description = "Product modified")
    @ApiResponse(responseCode = "403", description = "You don`t have permission with this user.")
    @ApiResponse(responseCode = "404", description = "Product not found with this id.")
    @ApiResponse(responseCode = "401", description = "Forbidden because you don't have permission with this account.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    suspend fun updateProduct(
        @AuthenticationPrincipal u: User,
        @PathVariable id: String, @Valid @RequestBody productoDto: ProductoCreateDto
    ): ResponseEntity<ProductoDto> = withContext(Dispatchers.IO) {
        logger.info { "Modify product with id $id" }
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
    @Operation(summary = "Delete Product", description = "Delete the product object.", tags = ["Super admin"])
    @Parameter(
        name = "id",
        description = "ID of Product",
        required = true,
        example = "08141138-eca0-4917-8406-4a6d327da14b"
    )
    @ApiResponse(responseCode = "204", description = "Product deleted.")
    @ApiResponse(responseCode = "403", description = "You don`t have permission with this user.")
    @ApiResponse(responseCode = "404", description = "Error to delete product with this id.")
    @ApiResponse(responseCode = "401", description = "Forbidden because you don't have permission with this account.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    suspend fun deleteProduct(@AuthenticationPrincipal u: User, @PathVariable id: String): ResponseEntity<ProductoDto> =
        withContext(Dispatchers.IO) {
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
     * Find all product paging: Find all the active product if the products existe paginated.
     *
     * @param page: number of the page
     * @param size: size of the page
     * @return Return all the products paginated.
     */
    @Operation(summary = "Find all paging", description = "Find all products paginated", tags = ["All users"])
    @Parameter(name = "page", description = "Number of the page", required = true, example = "0")
    @Parameter(name = "size", description = "Number of products on page", required = true, example = "1")
    @ApiResponse(responseCode = "200", description = "Products found.")
    @ApiResponse(responseCode = "404", description = "Productos not found")
    @GetMapping("paging")
    suspend fun findAllPagingUsers(
        @RequestParam(defaultValue = APIConfig.PAGINATION_INIT) page: Int,
        @RequestParam(defaultValue = APIConfig.PAGINATION_SIZE) size: Int
    ): ResponseEntity<ProductoPageDtoUser> {
        logger.info { "Find all products paginated" }
        val pageRequest = PageRequest.of(page, size)
        val pageResult = repository.findAllPage(pageRequest).firstOrNull()

        pageResult?.let {
            val dto = ProductoPageDtoUser(
                content = pageResult.content.filter { it.activo }.map { it.toDtoUser() },
                currentPage = pageResult.number,
                pageSize = pageResult.size,
            )
            return ResponseEntity.ok(dto)
        } ?: run {
            return ResponseEntity.notFound().build()
        }
    }

    /**
     * Find all product paging - Admins: Find all the product if the products existe paginated with details.
     *
     * @param page: number of the page
     * @param size: size of the page
     * @return Return all the products paginated.
     */
    @Operation(summary = "Find all paging", description = "Find all products paginated", tags = ["Super admin"])
    @Parameter(name = "page", description = "Number of the page", required = true, example = "0")
    @Parameter(name = "size", description = "Number of products on page", required = true, example = "1")
    @ApiResponse(responseCode = "200", description = "Products found.")
    @ApiResponse(responseCode = "403", description = "You don`t have permission with this user.")
    @ApiResponse(responseCode = "404", description = "Productos not found")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/admin/paging")
    suspend fun findAllPagingAdmins(
        @AuthenticationPrincipal u: User,
        @RequestParam(defaultValue = APIConfig.PAGINATION_INIT) page: Int,
        @RequestParam(defaultValue = APIConfig.PAGINATION_SIZE) size: Int
    ): ResponseEntity<ProductoPageDto> {
        logger.info { "Find all products paginated with details" }
        val pageRequest = PageRequest.of(page, size)
        val pageResult = repository.findAllPage(pageRequest).firstOrNull()

        pageResult?.let {
            val dto = ProductoPageDto(
                content = pageResult.content.map { it.toDto() },
                currentPage = pageResult.number,
                pageSize = pageResult.size,
                totalPages = if (pageResult.totalElements % pageResult.size == 0L) pageResult.totalElements / pageResult.size else (pageResult.totalElements / pageResult.size) + 1,
                totalProductos = pageResult.totalElements
            )
            return ResponseEntity.ok(dto)
        } ?: run {
            return ResponseEntity.notFound().build()
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
}