package pedidosApi.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import org.koin.ktor.ext.inject
import pedidosApi.clients.ProductosClient
import pedidosApi.clients.UsuariosClient
import pedidosApi.dto.PedidoDto
import pedidosApi.extensions.receiveOrNull
import pedidosApi.models.Pedido
import pedidosApi.repositories.PedidosRepository

fun Application.pedidosRouting() = routing {
    val repository by inject<PedidosRepository>()
    val userClient by inject<UsuariosClient>()
    val productClient by inject<ProductosClient>()

    get("/pedidos") {
        call.respond(repository.getAll().toList())
    }
    get("/pedidos/{id}") {

        val id = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            "Missing id"
        )

        val pedido = repository.getById(id) ?: return@get call.respond(
            HttpStatusCode.NotFound,
            "Pedido not found"
        )

        call.respond(pedido)
    }

    post("/pedidos") {
        println("POST /pedidos")
        val pedido =
            call.receiveOrNull<PedidoDto>() ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing pedido")

        call.respond(pedido)
    }

    put("/pedidos") {
        val pedido = call.receiveNullable<PedidoDto>() ?: return@put call.respond(
            HttpStatusCode.BadRequest,
            "Missing pedido"
        )

        val usuario = userClient.getUsuario(pedido.usuario) ?: return@put call.respond(
            HttpStatusCode.BadRequest,
            "Missing usuario"
        )

        val productos = pedido.productos.map {
            productClient.getProducto(it) ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                "Missing producto with id : $it"
            )
        }

        val pedidoToSave = Pedido(
            usuario = usuario,
            productos = productos,
            total = productos.sumOf { it.precio }
        )

        repository.save(pedidoToSave)
        call.respond(pedido)
    }
    patch("/pedidos") {
        val pedido = call.receiveNullable<PedidoDto>() ?: return@patch call.respond(
            HttpStatusCode.BadRequest,
            "Missing pedido"
        )

        val usuario = userClient.getUsuario(pedido.usuario) ?: return@patch call.respond(
            HttpStatusCode.BadRequest,
            "Missing usuario"
        )

        val productos = pedido.productos.map {
            productClient.getProducto(it) ?: return@patch call.respond(
                HttpStatusCode.BadRequest,
                "Missing producto with id : $it"
            )
        }

        val pedidoToSave = Pedido(
            usuario = usuario,
            productos = productos,
            total = productos.sumOf { it.precio }
        )

        repository.update(pedidoToSave)
        call.respond(pedido)
    }
    delete("/pedidos/{id}") {
        val id = call.parameters["id"]
        if (id == null) call.respond(HttpStatusCode.BadRequest, "Missing id")

        repository.delete(id!!)
        call.respond(HttpStatusCode.OK, "Pedido deleted")
    }
}