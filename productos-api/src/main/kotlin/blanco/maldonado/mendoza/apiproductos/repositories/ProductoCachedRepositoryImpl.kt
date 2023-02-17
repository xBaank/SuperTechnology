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

@Repository
class ProductoCachedRepositoryImpl
@Autowired constructor(
    private val productosRepository: ProductosRepository
) : ProductoCacheRepository {

    init {
        logger.info { "Iniciando Repositorio caché de Productos" }
    }

    override suspend fun findAll(): Flow<Producto> {
        logger.info { "Repositorio de productos findAll" }
        return productosRepository.findAll()
    }

    @Cacheable("productos")
    override suspend fun findById(uuid: String): Producto? {
        logger.info { "Repositorio de productos findById con id $uuid" }
        return productosRepository.findByUuid(uuid).firstOrNull()
    }

    override suspend fun findByCategoria(categoria: String): Flow<Producto> {
        logger.info { "Repositorio de productos findByCategoria con categoría $categoria" }
        return productosRepository.findByCategoria(categoria)
    }

    @Cacheable("productos")
    override suspend fun findByNombre(nombre: String): Flow<Producto> {
        logger.info { "Repositorio de productos findByNombre con nombre $nombre" }
        return productosRepository.findByNombre(nombre)
    }

    @CachePut("productos")
    override suspend fun save(producto: Producto): Producto {
        logger.info { "Repositorio de productos save: $producto" }
        return productosRepository.save(producto)
    }

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