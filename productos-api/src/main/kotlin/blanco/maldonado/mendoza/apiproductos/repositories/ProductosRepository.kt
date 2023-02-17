package blanco.maldonado.mendoza.apiproductos.repositories

import blanco.maldonado.mendoza.apiproductos.model.Producto
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository


@Repository
interface ProductosRepository : CoroutineCrudRepository<Producto, String> {
    fun findByNombre(nombre: String): Flow<Producto>
    fun findByCategoria(categoria: String): Flow<Producto>
    fun findByUuid(id: String): Flow<Producto>
}

