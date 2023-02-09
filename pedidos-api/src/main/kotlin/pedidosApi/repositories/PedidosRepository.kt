package pedidosApi.repositories

import org.litote.kmongo.coroutine.CoroutineDatabase
import pedidosApi.models.Pedido

class PedidosRepository(private val client: CoroutineDatabase) {
    fun getAll() = client.getCollection<Pedido>().find().toFlow()
    suspend fun getById(id: String) = client.getCollection<Pedido>().findOneById(id)
    suspend fun save(pedido: Pedido) = client.getCollection<Pedido>().save(pedido)
    suspend fun delete(id: String) = client.getCollection<Pedido>().deleteOneById(id)

}