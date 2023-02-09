package pedidosApi.routing

import arrow.core.nonEmptyListOf
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
        val pedido = call.receiveOrNull<CreatePedidoDto>() ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            "Incorrect pedido"
        )

        val usuario = userClient.getUsuario(pedido.usuario) ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            "Missing usuario"
        )

        val productos = pedido.productos.map {
            productClient.getProducto(it) ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "Missing producto with id : $it"
            )
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

    put("/pedidos") {
        val pedido = call.receiveOrNull<CreatePedidoDto>() ?: return@put call.respond(
            HttpStatusCode.BadRequest,
            "Incorrect pedido"
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

        val pedidoToUpdate = Pedido(
            usuario = usuario,
            tareas = nonEmptyListOf(
                Tarea(
                    productos = productos,
                    empleado = usuario
                )
            )
        )

        repository.save(pedidoToUpdate)
        call.respond(pedidoToUpdate.toDto())
    }

    delete("/pedidos/{id}") {
        val id = call.parameters["id"]
        if (id == null) call.respond(HttpStatusCode.BadRequest, "Missing id")

        repository.delete(id!!)
        call.respond(HttpStatusCode.OK, "Pedido deleted")
    }
}