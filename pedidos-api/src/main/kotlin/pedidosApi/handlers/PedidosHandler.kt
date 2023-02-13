package pedidosApi.handlers

import arrow.core.Either
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.*
import pedidosApi.exceptions.PedidoError
import pedidosApi.json.buildErrorJson
import pedidosApi.json.buildPagedPedidoJson
import pedidosApi.json.buildPedidoDto
import pedidosApi.models.Pedido
import pedidosApi.repositories.PagedFlow

typealias ApplicationCallContext = PipelineContext<*, ApplicationCall>

@JvmName("handleResultFlow")
suspend fun ApplicationCallContext.handleResult(pedidoResult: Either<PedidoError, Flow<Pedido>>) =
    pedidoResult.fold(ifLeft = { handleError(it) }, ifRight = { call.respond(it.map(::buildPedidoDto).toList()) })

@JvmName("handleResultPagedFlow")
suspend fun ApplicationCallContext.handleResult(pedidoResult: Either<PedidoError, PagedFlow<Pedido>>) =
    pedidoResult.fold(ifLeft = { handleError(it) }, ifRight = { call.respond(buildPagedPedidoJson(it)) })

@JvmName("handleResultPedido")
suspend fun ApplicationCallContext.handleResult(pedidoResult: Either<PedidoError, Pedido>) =
    pedidoResult.fold(ifLeft = { handleError(it) }, ifRight = { call.respond(buildPedidoDto(it)) })

@JvmName("handleResultUnit")
suspend fun ApplicationCallContext.handleResult(pedidoResult: Either<PedidoError, Unit>) =
    pedidoResult.fold(ifLeft = { handleError(it) }, ifRight = { call.respond(HttpStatusCode.NoContent) })

suspend fun ApplicationCallContext.handleError(error: PedidoError) = when (error) {
    is PedidoError.PedidoNotFound -> call.respond(
        HttpStatusCode.NotFound,
        buildErrorJson(error.message, HttpStatusCode.NotFound.value)
    )

    is PedidoError.InvalidPedidoId -> call.respond(
        HttpStatusCode.BadRequest,
        buildErrorJson(error.message, HttpStatusCode.BadRequest.value)
    )

    is PedidoError.PedidoSaveError -> call.respond(
        HttpStatusCode.InternalServerError,
        buildErrorJson(error.message, HttpStatusCode.InternalServerError.value)
    )

    is PedidoError.InvalidPedidoPage -> call.respond(
        HttpStatusCode.BadRequest,
        buildErrorJson(error.message, HttpStatusCode.BadRequest.value)
    )

    is PedidoError.InvalidPedidoFormat -> call.respond(
        HttpStatusCode.BadRequest,
        buildErrorJson(error.message, HttpStatusCode.BadRequest.value)
    )

    is PedidoError.MissingPedidoId -> call.respond(
        HttpStatusCode.BadRequest,
        buildErrorJson(error.message, HttpStatusCode.BadRequest.value)
    )
}
