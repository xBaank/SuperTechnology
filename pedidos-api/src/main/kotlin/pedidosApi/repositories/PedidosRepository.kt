package pedidosApi.repositories

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.id.toId
import pedidosApi.dto.UsuarioDto
import pedidosApi.exceptions.PedidoError
import pedidosApi.exceptions.PedidoError.*
import pedidosApi.extensions.toObjectIdOrNull
import pedidosApi.models.Pedido

class PedidosRepository(client: CoroutineDatabase) {
    private val collection = client.getCollection<Pedido>()

    fun getByPage(page: Int, size: Int): Either<PedidoError, PagedFlow<Pedido>> {
        if (page < 0) return InvalidPedidoPage("Page must be greater or equal than 0").left()
        if (size < 1) return InvalidPedidoPage("Size must be greater than 0").left()

        val flow = collection.find().skip(page * size).limit(size).toFlow()
        return PagedFlow(page, size, flow).right()
    }

    suspend fun getById(id: String): Either<PedidoError, Pedido> {
        val _id = id.toObjectIdOrNull()?.toId<Pedido>()
            ?: return InvalidPedidoId("id : '$id' format is incorrect").left()

        return collection.find(Pedido::_id eq _id).first()?.right()
            ?: PedidoNotFound("Pedido with with id : '${id}' not found").left()
    }

    fun getByUserId(id: String, page: Int, size: Int): Either<PedidoError, PagedFlow<Pedido>> {
        if (page < 0) return InvalidPedidoPage("Page must be greater or equal than 0").left()
        if (size < 1) return InvalidPedidoPage("Size must be greater than 0").left()

        val flow = collection.find(Pedido::usuario / UsuarioDto::id eq id).skip(page * size).limit(size).toFlow()
        return PagedFlow(page, size, flow).right()
    }

    suspend fun save(pedido: Pedido): Either<PedidoError, Pedido> {
        val result = collection.save(pedido) ?: return pedido.right()
        if (!result.wasAcknowledged()) return PedidoSaveError("Couldn't save pedido with id : '${pedido._id}'").left()
        return pedido.right()
    }

    suspend fun delete(id: String): Either<PedidoError, Unit> {
        val _id = id.toObjectIdOrNull()?.toId<Pedido>()
            ?: return InvalidPedidoId("id : '$id' format is incorrect").left()

        val result = collection.deleteOneById(_id)
        if (!result.wasAcknowledged()) return PedidoSaveError("Couldn't delete pedido with id : '${id}'").left()
        return Unit.right()
    }

}