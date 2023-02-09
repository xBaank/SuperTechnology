package pedidosApi.repositories

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.toId
import pedidosApi.models.Pedido

class PedidosRepository(private val client: CoroutineDatabase) {
    fun getAll() = client.getCollection<Pedido>().find().toFlow()
    suspend fun getById(id: String) = client.getCollection<Pedido>().findOneById(id.toId<String>())
    suspend fun save(pedido: Pedido) = client.getCollection<Pedido>().save(pedido)
    suspend fun delete(id: String) = client.getCollection<Pedido>().deleteOneById(id)

}