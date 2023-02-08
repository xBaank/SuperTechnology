package blanco.maldonado.mendoza.apiproductos.repository

import blanco.maldonado.mendoza.apiproductos.model.Producto
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID


@Repository
interface ProductosRepository : CoroutineCrudRepository<Producto, UUID> {

    fun findByNombre(nombre: String): Flow<Producto>

    fun findByCategoria(categoria: String): Flow<Producto>
    
}

