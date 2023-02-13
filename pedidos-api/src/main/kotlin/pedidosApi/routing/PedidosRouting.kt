package pedidosApi.routing

import arrow.core.Either
import arrow.core.continuations.either
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.ktor.ext.inject
import org.litote.kmongo.Id
import org.litote.kmongo.id.toId
import org.litote.kmongo.newId
import pedidosApi.clients.ProductosClient
import pedidosApi.clients.UsuariosClient
import pedidosApi.dto.CreatePedidoDto
import pedidosApi.exceptions.DomainError
import pedidosApi.exceptions.PedidoError.InvalidPedidoFormat
import pedidosApi.exceptions.PedidoError.InvalidPedidoId
import pedidosApi.extensions.inject
import pedidosApi.extensions.mapToApiError
import pedidosApi.extensions.receiveOrNull
import pedidosApi.extensions.toObjectIdOrNull
import pedidosApi.handlers.handleError
import pedidosApi.handlers.handleResult
import pedidosApi.models.Pedido
import pedidosApi.models.Tarea
import pedidosApi.repositories.PedidosRepository

fun Routing.pedidosRouting() = route("/pedidos") {
    val repository by inject<PedidosRepository>()

    get("/usuario/{id}") {
        val usuarioId = call.parameters.getOrFail("id")
        handleResult(repository.getByUserId(usuarioId))
    }

    get {
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
        handleResult(repository.getByPage(page, size))
    }

    get("{id}") {
        val id = call.parameters.getOrFail("id")
        handleResult(repository.getById(id))
    }

    post {
        val pedido = call.receiveOrNull<CreatePedidoDto>()
            ?: return@post handleError(InvalidPedidoFormat("Invalid body format"))

        createPedido(pedido).fold(
            ifLeft = { handleError(it) },
            ifRight = { handleResult(repository.save(it)) }
        )
    }

    put("{id}") {
        val id = call.parameters.getOrFail("id").toObjectIdOrNull()?.toId<Pedido>()
            ?: return@put handleError(InvalidPedidoId("Invalid id format"))

        val pedido = call.receiveOrNull<CreatePedidoDto>()
            ?: return@put handleError(InvalidPedidoFormat("Invalid body format"))

        createPedido(pedido, id).fold(
            ifLeft = { handleError(it) },
            ifRight = { handleResult(repository.save(it)) }
        )
    }

    delete("{id}") {
        val id = call.parameters.getOrFail("id")
        handleResult(repository.delete(id))
    }
}

private suspend fun createPedido(
    pedido: CreatePedidoDto,
    id: Id<Pedido> = newId()
): Either<DomainError, Pedido> = either {
    val userClient by inject<UsuariosClient>()
    val productClient by inject<ProductosClient>()

    val usuario = userClient.getUsuario(pedido.usuario).mapToApiError().bind()

    val productos = pedido.productos.map {
        productClient.getProducto(it).mapToApiError().bind()
    }

    Pedido(
        _id = id,
        usuario = usuario,
        tareas = listOf(
            Tarea(productos = productos, empleado = usuario)
        )
    )
}