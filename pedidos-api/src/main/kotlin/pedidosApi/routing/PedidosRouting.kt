package pedidosApi.routing

import arrow.core.nonEmptyListOf
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.koin.ktor.ext.inject
import pedidosApi.clients.ProductosClient
import pedidosApi.clients.UsuariosClient
import pedidosApi.dto.CreatePedidoDto
import pedidosApi.extensions.receiveOrNull
import pedidosApi.extensions.toDto
import pedidosApi.models.Pedido
import pedidosApi.models.Tarea
import pedidosApi.repositories.PedidosRepository

fun Routing.pedidosRouting() = route("/pedidos") {
    val repository by inject<PedidosRepository>()
    val userClient by inject<UsuariosClient>()
    val productClient by inject<ProductosClient>()

    get {
        call.respond(repository.getAll().map(Pedido::toDto).toList())
    }
    get("{id}") {
        val id = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            "Missing id"
        )

        val pedido = repository.getById(id) ?: return@get call.respond(
            HttpStatusCode.NotFound,
            "Pedido not found"
        )

        call.respond(pedido.toDto())
    }

    post {
        save(userClient, productClient, repository)
    }

    put {
        save(userClient, productClient, repository)
    }

    delete("{id}") {
        val id = call.parameters["id"] ?: return@delete call.respond(
            HttpStatusCode.BadRequest,
            "Missing id"
        )

        repository.delete(id)
        call.respond(HttpStatusCode.OK, "Pedido deleted")
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.save(
    userClient: UsuariosClient,
    productClient: ProductosClient,
    repository: PedidosRepository
) {
    val pedido = call.receiveOrNull<CreatePedidoDto>() ?: return call.respond(
        HttpStatusCode.BadRequest,
        "Incorrect pedido"
    )

    val usuario = userClient.getUsuario(pedido.usuario)

    val productos = pedido.productos.map {
        productClient.getProducto(it)
    }

    val pedidoToSave = Pedido(
        usuario = usuario,
        tareas = nonEmptyListOf(
            Tarea(
                productos = productos,
                empleado = usuario
            )
        )
    )


    repository.save(pedidoToSave)
    call.respond(pedidoToSave.toDto())
}