/**
 * @since 16/02/2023
 * @author Azahara Blanco, Alfredo Maldonado, Sebastian Mendoza
 */

package blanco.maldonado.mendoza.apiproductos.repositories

import blanco.maldonado.mendoza.apiproductos.model.Producto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Producto cached repository impl
 *
 * @property productosRepository
 * @constructor Create empty Producto cached repository impl
 */
@Repository
class ProductoCachedRepositoryImpl
@Autowired constructor(
    private val productosRepository: ProductosRepository
) : ProductoCacheRepository {

    init {
        logger.info { "Iniciando Repositorio caché de Productos" }
    }

    /**
     * Find all: Find all repositories.
     *
     * @return All the products.
     */
    override suspend fun findAll(): Flow<Producto> {
        logger.info { "Repositorio de productos findAll" }
        return productosRepository.findAll()
    }

    /**
     * Find by id: Find by id the product.
     *
     * @param uuid
     * @return One product
     */
    @Cacheable("productos")
    override suspend fun findById(uuid: String): Producto? {
        logger.info { "Repositorio de productos findById con id $uuid" }
        return productosRepository.findByUuid(uuid).firstOrNull()
    }

    /**
     * Find by categoria: Find the product by id of category.
     *
     * @param categoria
     * @return The product.
     */
    override suspend fun findByCategoria(categoria: String): Flow<Producto> {
        logger.info { "Repositorio de productos findByCategoria con categoría $categoria" }
        return productosRepository.findByCategoria(categoria)
    }

    /**
     * Find by nombre: Find the product by id of Nombre.
     *
     * @param nombre
     * @return The product.
     */
    @Cacheable("productos")
    override suspend fun findByNombre(nombre: String): Flow<Producto> {
        logger.info { "Repositorio de productos findByNombre con nombre $nombre" }
        return productosRepository.findByNombre(nombre)
    }

    /**
     * Save: Save the product.
     *
     * @param producto
     * @return The product that was saved.
     */
    @CachePut("productos")
    override suspend fun save(producto: Producto): Producto {
        logger.info { "Repositorio de productos save: $producto" }
        return productosRepository.save(producto)
    }

    /**
     * Update: Modified thr product.
     *
     * @param uuid
     * @param producto
     * @return The product modified.
     */
    @CachePut("productos")
    override suspend fun update(uuid: String, producto: Producto): Producto? {
        logger.info { "Repositorio de productos update " }
        val productoUUID = productosRepository.findByUuid(uuid).firstOrNull()
        productoUUID?.let {
            val updated = it.copy(
                uuid = it.uuid,
                nombre = producto.nombre,
                categoria = producto.categoria,
                stock = producto.stock,
                description = producto.description,
                createdAt = it.createdAt,
                updateAt = LocalDateTime.now(),
                deleteAt = producto.deleteAt,
                precio = producto.precio,
                activo = producto.activo
            )
            return productosRepository.save(updated)
        }
        return null
    }

    /**
     * Delete: Delete the product
     *
     * @param uuid
     * @return The product that was deleted.
     */
    @CacheEvict("productos")
    override suspend fun delete(uuid: String): Producto? {
        logger.info { "Repositorio de productos delete" }
        val productoDelete = productosRepository.findByUuid(uuid).firstOrNull()
        productoDelete.let {
            productosRepository.delete(it!!)
        }
        return null
    }
}