/**
 * @since 16/02/2023
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
package blanco.maldonado.mendoza.apiproductos.repositories

import blanco.maldonado.mendoza.apiproductos.model.Producto
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

interface ProductoCacheRepository {
    suspend fun findAll(): Flow<Producto>
    suspend fun findById(uuid: String): Producto?
    suspend fun findByCategoria(categoria: String): Flow<Producto>
    suspend fun findByNombre(nombre: String): Flow<Producto>
    suspend fun save(producto: Producto): Producto
    suspend fun update(uuid: String, producto: Producto): Producto?
    suspend fun delete(uuid: String): Producto?
    suspend fun findAllPage(pageRequest: PageRequest): Flow<Page<Producto>>
}