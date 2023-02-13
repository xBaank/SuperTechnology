package pedidosApi.routing

import arrow.core.continuations.either
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.ktor.ext.inject
import org.litote.kmongo.Id
import org.litote.kmongo.id.toId
import pedidosApi.clients.ProductosClient
import pedidosApi.clients.UsuariosClient
import pedidosApi.dto.CreatePedidoDto
import pedidosApi.dto.UpdatePedidoDto
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

const val DEFAULT_PAGE = 0
const val DEFAULT_SIZE = 10

fun Routing.pedidosRouting() = route("/pedidos") {
    val repository by inject<PedidosRepository>()

    get("/usuario/{id}") {
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: DEFAULT_SIZE

        val usuarioId = call.parameters.getOrFail("id")
        handleResult(repository.getByUserId(usuarioId, page, size))
    }

    get {
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: DEFAULT_SIZE
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

        val pedido = call.receiveOrNull<UpdatePedidoDto>()

        updatePedido(pedido, id).fold(
            ifLeft = { handleError(it) },
            ifRight = { handleResult(repository.save(it)) }
        )
    }

    delete("{id}") {
        val id = call.parameters.getOrFail("id")
        handleResult(repository.delete(id))
    }
}

private suspend fun createPedido(pedido: CreatePedidoDto) = either {
    val userClient by inject<UsuariosClient>()
    val productClient by inject<ProductosClient>()


    val usuario = userClient.getUsuario(pedido.usuario).mapToApiError().bind()

    val productos = pedido.productos.map {
        productClient.getProducto(it).mapToApiError().bind()
    }

    val current = System.currentTimeMillis()

    Pedido(
        usuario = usuario,
        tareas = listOf(
            Tarea(productos = productos, empleado = usuario, createdAt = current)
        ),
        iva = pedido.iva,
        estado = "PENDIENTE",
        createdAt = current
    )
}

/**
 * Update user, iva and estado
 */
private suspend fun updatePedido(updatePedidoDto: UpdatePedidoDto?, id: Id<Pedido>) = either {
    val userClient by inject<UsuariosClient>()
    val pedidosRepository by inject<PedidosRepository>()

    val pedidoToUpdate = pedidosRepository.getById(id.toString()).bind()
    val usuario = userClient.getUsuario(pedidoToUpdate.usuario.id).mapToApiError().bind()

    pedidoToUpdate.copy(
        usuario = usuario,
        iva = updatePedidoDto?.iva ?: pedidoToUpdate.iva,
        estado = updatePedidoDto?.estado ?: pedidoToUpdate.estado
    )
}