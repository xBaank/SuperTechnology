package pedidosApi.routing

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.github.smiley4.ktorswaggerui.dsl.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.ktor.ext.inject
import org.litote.kmongo.id.toId
import pedidosApi.buildErrorDto
import pedidosApi.buildPagedPedidoDto
import pedidosApi.buildPedidoDto
import pedidosApi.clients.ProductosClient
import pedidosApi.clients.UsuariosClient
import pedidosApi.dto.requests.CreatePedidoDto
import pedidosApi.dto.requests.UpdatePedidoDto
import pedidosApi.exceptions.ApiError
import pedidosApi.exceptions.DomainError
import pedidosApi.exceptions.PedidoError
import pedidosApi.extensions.inject
import pedidosApi.extensions.mapToApiError
import pedidosApi.extensions.receiveOrNull
import pedidosApi.extensions.toObjectIdOrNull
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

            repository.getByUserId(userId, page, size)
                .map { buildPagedPedidoDto(it) }
                .onLeft { call.handleError(it) }
                .onRight { call.respond(it) }
        }
    }
    authenticate("admin") {
        get("/usuario/{id}", builder = OpenApiRoute::getByUsuarioId) {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: DEFAULT_SIZE
            val usuarioId = call.parameters.getOrFail("id")

            repository.getByUserId(usuarioId, page, size)
                .map { buildPagedPedidoDto(it) }
                .onLeft { call.handleError(it) }
                .onRight { call.respond(it) }
        }

        get(builder = OpenApiRoute::getAll) {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: DEFAULT_PAGE
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: DEFAULT_SIZE

            repository.getByPage(page, size)
                .map { buildPagedPedidoDto(it) }
                .onLeft { call.handleError(it) }
                .onRight { call.respond(it) }
        }

        get("{id}", builder = OpenApiRoute::getById) {
            val id = call.parameters.getOrFail("id")

            repository.getById(id)
                .map { buildPedidoDto(it) }
                .onLeft { call.handleError(it) }
                .onRight { call.respond(HttpStatusCode.OK, it) }
        }

        post(builder = OpenApiRoute::post) {
            val pedido = call.receiveOrNull<CreatePedidoDto>()
                .let { it?.right() ?: PedidoError.InvalidPedidoFormat("Invalid body format").left() }

            pedido.flatMap { createPedido(it) }
                .flatMap { repository.save(it) }
                .map { buildPedidoDto(it) }
                .onLeft { call.handleError(it) }
                .onRight { call.respond(HttpStatusCode.Created, it) }
        }

        put("{id}", builder = OpenApiRoute::put) {
            val id = call.parameters.getOrFail("id")
            val pedido = call.receiveOrNull<UpdatePedidoDto>()

            updatePedido(pedido, id)
                .flatMap { repository.save(it) }
                .map { buildPedidoDto(it) }
                .onLeft { call.handleError(it) }
                .onRight { call.respond(HttpStatusCode.OK, it) }
        }

        delete("{id}", builder = OpenApiRoute::delete) {
            val id = call.parameters.getOrFail("id")

            repository.delete(id)
                .onLeft { call.handleError(it) }
                .onRight { call.respond(HttpStatusCode.NoContent) }
        }
    }
}

private suspend fun createPedido(
    pedido: CreatePedidoDto,
): Either<ApiError, Pedido> = either {
    val userClient by inject<UsuariosClient>()
    val productoClient by inject<ProductosClient>()


    val usuario = userClient.getUsuario(pedido.usuario).mapToApiError().bind()

    val current = System.currentTimeMillis()

    val tareas = pedido.tareas.map { tarea ->
        Tarea(
            producto = productoClient.getProducto(tarea.producto).mapToApiError().bind(),
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
private suspend fun updatePedido(updatePedidoDto: UpdatePedidoDto?, id: String): Either<DomainError, Pedido> = either {
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

suspend fun ApplicationCall.handleError(error: DomainError) = when (error) {
    is PedidoError.PedidoNotFound -> respond(
        HttpStatusCode.NotFound,
        buildErrorDto(error.message, HttpStatusCode.NotFound.value)
    )

    is PedidoError.InvalidPedidoId -> respond(
        HttpStatusCode.BadRequest,
        buildErrorDto(error.message, HttpStatusCode.BadRequest.value)
    )

    is PedidoError.PedidoSaveError -> respond(
        HttpStatusCode.NotFound,
        buildErrorDto(error.message, HttpStatusCode.NotFound.value)
    )

    is PedidoError.InvalidPedidoPage -> respond(
        HttpStatusCode.BadRequest,
        buildErrorDto(error.message, HttpStatusCode.BadRequest.value)
    )

    is PedidoError.InvalidPedidoFormat -> respond(
        HttpStatusCode.BadRequest,
        buildErrorDto(error.message, HttpStatusCode.BadRequest.value)
    )

    is PedidoError.MissingPedidoId -> respond(
        HttpStatusCode.BadRequest,
        buildErrorDto(error.message, HttpStatusCode.BadRequest.value)
    )

    is ApiError -> respond(
        HttpStatusCode.FailedDependency,
        buildErrorDto(error.message, HttpStatusCode.FailedDependency.value)
    )
}