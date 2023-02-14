package blanco.maldonado.mendoza.apiproductos.repositories

import blanco.maldonado.mendoza.apiproductos.model.Producto
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository


@Repository
interface ProductosRepository : CoroutineCrudRepository<Producto, Int> {

    fun findByNombre(nombre: String): Flow<Producto>
    fun findByCategoria(categoria: String): Flow<Producto>

}

