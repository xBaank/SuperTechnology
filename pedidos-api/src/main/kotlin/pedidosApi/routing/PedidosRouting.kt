package pedidosApi.routing

import arrow.core.continuations.either
import arrow.core.getOrElse
import io.github.smiley4.ktorswaggerui.dsl.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.ktor.ext.inject
import org.litote.kmongo.id.toId
import pedidosApi.clients.ProductosClient
import pedidosApi.clients.UsuariosClient
import pedidosApi.dto.requests.CreatePedidoDto
import pedidosApi.dto.requests.UpdatePedidoDto
import pedidosApi.exceptions.PedidoError
import pedidosApi.extensions.inject
import pedidosApi.extensions.mapToApiError
import pedidosApi.extensions.receiveOrNull
import pedidosApi.extensions.toObjectIdOrNull
import pedidosApi.handlers.handleError
import pedidosApi.handlers.handleResult
import pedidosApi.models.EstadoPedido
import pedidosApi.models.Pedido
import pedidosApi.models.Tarea
import pedidosApi.repositories.PedidosRepository

const val DEFAULT_PAGE = 0
const val DEFAULT_SIZE = 10

fun Routing.pedidosRouting() = route("/pedidos") {
    val repository by inject<PedidosRepository>()
    authenticate("user") {
        get("/usuario/me", builder = OpenApiRoute::getByUsuarioId) {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: DEFAULT_SIZE
            val userId = call.principal<JWTPrincipal>()?.getClaim("id", String::class) ?: ""
            repository.getByUserId(userId, page, size).getOrElse { throw it }
        }
    }
    authenticate("admin") {
        get("/usuario/{id}", builder = OpenApiRoute::getByUsuarioId) {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: DEFAULT_SIZE

            val usuarioId = call.parameters.getOrFail("id")
            handleResult(repository.getByUserId(usuarioId, page, size))
        }

        get(builder = OpenApiRoute::getAll) {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: DEFAULT_SIZE
            handleResult(repository.getByPage(page, size))
        }

        get("{id}", builder = OpenApiRoute::getById) {
            val id = call.parameters.getOrFail("id")
            handleResult(repository.getById(id), HttpStatusCode.OK)
        }

        post(builder = OpenApiRoute::post) {
            val pedido = call.receiveOrNull<CreatePedidoDto>()
                ?: return@post handleError(PedidoError.InvalidPedidoFormat("Invalid body format"))

            createPedido(pedido).fold(
                ifLeft = { handleError(it) },
                ifRight = { handleResult(repository.save(it), HttpStatusCode.Created) }
            )
        }

        put("{id}", builder = OpenApiRoute::put) {
            val id = call.parameters.getOrFail("id")
            val pedido = call.receiveOrNull<UpdatePedidoDto>()

            updatePedido(pedido, id).fold(
                ifLeft = { handleError(it) },
                ifRight = { handleResult(repository.save(it), HttpStatusCode.OK) }
            )
        }

        delete("{id}", builder = OpenApiRoute::delete) {
            val id = call.parameters.getOrFail("id")
            handleResult(repository.delete(id))
        }
    }
}

private suspend fun createPedido(pedido: CreatePedidoDto) = either {
    val userClient by inject<UsuariosClient>()
    val productClient by inject<ProductosClient>()


    val usuario = userClient.getUsuario(pedido.usuario).mapToApiError().bind()

    val current = System.currentTimeMillis()

    val tareas = pedido.tareas.map { tarea ->
        Tarea(
            producto = productClient.getProducto(tarea.producto).mapToApiError().bind(),
            empleado = usuario,
            createdAt = current
        )
    }


    Pedido(
        usuario = usuario,
        tareas = tareas,
        iva = pedido.iva,
        estado = EstadoPedido.EN_PROCESO,
        createdAt = current
    )
}

/**
 * Update user, iva and estado
 */
private suspend fun updatePedido(updatePedidoDto: UpdatePedidoDto?, id: String) = either {
    val userClient by inject<UsuariosClient>()
    val pedidosRepository by inject<PedidosRepository>()

    val _id = id.toObjectIdOrNull()?.toId<Pedido>()
        ?: shift(PedidoError.InvalidPedidoId("Invalid id format"))

    val pedidoToUpdate = pedidosRepository.getById(_id.toString()).bind()
    val usuario = userClient.getUsuario(pedidoToUpdate.usuario.id).mapToApiError().bind()

    pedidoToUpdate.copy(
        usuario = usuario,
        iva = updatePedidoDto?.iva ?: pedidoToUpdate.iva,
        estado = updatePedidoDto?.estado ?: pedidoToUpdate.estado
    )
}