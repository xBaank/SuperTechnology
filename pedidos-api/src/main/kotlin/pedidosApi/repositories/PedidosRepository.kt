package pedidosApi.repositories

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.flow.Flow
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.id.toId
import pedidosApi.dto.responses.UsuarioDto
import pedidosApi.exceptions.PedidoError
import pedidosApi.extensions.toObjectIdOrNull
import pedidosApi.models.Pedido

const val MAX_SIZE = 500

class PagedFlow<T>(val page: Int, val size: Int, results: Flow<T>) : Flow<T> by results

class PedidosRepository(private val collection: CoroutineCollection<Pedido>) {

    suspend fun getByPage(page: Int, size: Int): Either<PedidoError, PagedFlow<Pedido>> = either {
        validatePage(page, size).bind()

        val flow = collection.find().skip(page * size).limit(size).toFlow()
        PagedFlow(page, size, flow)
    }

    suspend fun getById(id: String): Either<PedidoError, Pedido> {
        val _id = id.toObjectIdOrNull()?.toId<Pedido>()
            ?: return PedidoError.InvalidPedidoId("id : '$id' format is incorrect").left()

        return collection.find(Pedido::_id eq _id).first()?.right()
            ?: PedidoError.PedidoNotFound("Pedido with with id : '${id}' not found").left()
    }

    suspend fun getByUsername(username: String, page: Int, size: Int): Either<PedidoError, PagedFlow<Pedido>> = either {
        validatePage(page, size).bind()

        val flow =
            collection.find(Pedido::usuario / UsuarioDto::username eq username).skip(page * size).limit(size).toFlow()
        PagedFlow(page, size, flow)
    }

    suspend fun save(pedido: Pedido): Either<PedidoError, Pedido> {
        val result = collection.save(pedido) ?: return pedido.right()
        if (!result.wasAcknowledged()) return PedidoError.PedidoSaveError("Couldn't save pedido with id : '${pedido._id}'")
            .left()
        return pedido.right()
    }

    suspend fun delete(id: String): Either<PedidoError, Unit> {
        val _id = id.toObjectIdOrNull()?.toId<Pedido>()
            ?: return PedidoError.InvalidPedidoId("id : '$id' format is incorrect").left()

        val result = collection.deleteOneById(_id)

        if (!result.wasAcknowledged())
            return PedidoError.PedidoSaveError("Couldn't delete pedido with id : '${id}'").left()

        if (result.deletedCount == 0L)
            return PedidoError.PedidoSaveError("No pedidos deleted with id : '${id}'").left()

        return Unit.right()
    }

    private fun validatePage(page: Int, size: Int): Either<PedidoError.InvalidPedidoPage, Unit> {
        if (page < 0) return PedidoError.InvalidPedidoPage("Page must be greater or equal than 0").left()
        if (size < 1) return PedidoError.InvalidPedidoPage("Size must be greater than 0").left()
        if (size >= MAX_SIZE) return PedidoError.InvalidPedidoPage("Size must be less or equal than $MAX_SIZE").left()
        return Unit.right()
    }
}