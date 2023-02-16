package blanco.maldonado.mendoza.apiproductos.repositories

import blanco.maldonado.mendoza.apiproductos.model.Producto
import kotlinx.coroutines.flow.Flow

interface ProductoCacheRepository {
    suspend fun findAll(): Flow<Producto>
    suspend fun findById(id: String): Producto?
    suspend fun findByCategoria(categoria: String): Flow<Producto>
    suspend fun findByNombre(nombre: String): Flow<Producto>
    suspend fun save(producto: Producto): Producto
    suspend fun update(id: String, producto: Producto): Producto?
    suspend fun delete(id: String): Producto?
}