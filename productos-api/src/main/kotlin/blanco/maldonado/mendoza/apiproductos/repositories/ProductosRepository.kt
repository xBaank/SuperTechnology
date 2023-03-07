/**
 * @since 16/02/2023
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
package blanco.maldonado.mendoza.apiproductos.repositories

import blanco.maldonado.mendoza.apiproductos.model.Producto
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductosRepository : CoroutineCrudRepository<Producto, String> {
    fun findByNombre(nombre: String): Flow<Producto>
    fun findByCategoria(categoria: String): Flow<Producto>
    fun findByUuid(id: String): Flow<Producto>
    fun findAllBy(pageable: Pageable?): Flow<Producto>
}

