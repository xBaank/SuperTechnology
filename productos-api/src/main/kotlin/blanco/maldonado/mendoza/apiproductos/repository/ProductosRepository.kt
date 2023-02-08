package blanco.maldonado.mendoza.apiproductos.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.Flow

@Repository
interface ProductosRepository : CoroutineCrudRepository<Producto, UUID> {
    fun findByNombre(nombre: String): Flow<Producto>

    fun findByCategoria(categoria: String): Flow<Producto>

    fun findByUuid(uuid: String): Flow<Producto>
}

