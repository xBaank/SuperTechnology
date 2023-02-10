package pedidosApi.repositories

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.id.toId
import pedidosApi.extensions.toObjectIdOrNull
import pedidosApi.models.Pedido

class PedidosRepository(private val client: CoroutineDatabase) {
    fun getAll() = client.getCollection<Pedido>().find().toFlow()
    suspend fun getById(id: String): Pedido? {
        val _id = id.toObjectIdOrNull()?.toId<Pedido>() ?: return null
        return client.getCollection<Pedido>().find(Pedido::_id eq _id).first()
    }

    suspend fun save(pedido: Pedido) = client.getCollection<Pedido>().save(pedido)
    suspend fun delete(id: String) = client.getCollection<Pedido>().deleteOneById(id)

}