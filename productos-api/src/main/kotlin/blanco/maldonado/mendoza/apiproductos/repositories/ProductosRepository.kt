package blanco.maldonado.mendoza.apiproductos.repositories

import blanco.maldonado.mendoza.apiproductos.model.Producto
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface ProductosRepository : CoroutineCrudRepository<Producto, UUID> {

    fun findByNombre(nombre: String): Producto

    fun findByCategoria(categoria: String): Flow<Producto>

}

