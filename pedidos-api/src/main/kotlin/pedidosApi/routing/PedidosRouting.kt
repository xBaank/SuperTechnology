package pedidosApi.routing

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
import pedidosApi.exceptions.PedidoError.InvalidPedidoFormat
import pedidosApi.exceptions.PedidoError.InvalidPedidoId
import pedidosApi.extensions.inject
import pedidosApi.extensions.receiveOrNull
import pedidosApi.extensions.toObjectIdOrNull
import pedidosApi.handlers.handleError
import pedidosApi.handlers.handleResult
import pedidosApi.models.Pedido
import pedidosApi.models.Tarea
import pedidosApi.repositories.PedidosRepository

fun Routing.pedidosRouting() = route("/pedidos") {
    val repository by inject<PedidosRepository>()


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

        val pedidoToSave = createPedido(pedido)

        handleResult(repository.save(pedidoToSave))
    }

    put("{id}") {
        val id = call.parameters.getOrFail("id").toObjectIdOrNull()?.toId<Pedido>()
            ?: return@put handleError(InvalidPedidoId("Invalid id format"))

        val pedido = call.receiveOrNull<CreatePedidoDto>()
            ?: return@put handleError(InvalidPedidoFormat("Invalid body format"))

        val pedidoToUpdate = createPedido(pedido, id)

        handleResult(repository.save(pedidoToUpdate))
    }

    delete("{id}") {
        val id = call.parameters.getOrFail("id")
        handleResult(repository.delete(id))
    }
}

private suspend fun createPedido(
    pedido: CreatePedidoDto,
    id: Id<Pedido> = newId()
): Pedido {
    val userClient by inject<UsuariosClient>()
    val productClient by inject<ProductosClient>()

    val usuario = userClient.getUsuario(pedido.usuario)

    val productos = pedido.productos.map {
        productClient.getProducto(it)
    }

    return Pedido(
        _id = id,
        usuario = usuario,
        tareas = listOf(
            Tarea(
                productos = productos,
                empleado = usuario
            )
        )
    )
}