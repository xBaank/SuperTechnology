package pedidosApi.validators

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import pedidosApi.exceptions.ApiError
import pedidosApi.exceptions.DomainError
import pedidosApi.exceptions.PedidoError
import pedidosApi.json.buildErrorDto

fun Application.configureValidation() {
    install(StatusPages) {
        exception<DomainError> { call, cause -> handleError(call, cause) }
    }
}

suspend fun handleError(call: ApplicationCall, error: DomainError) = when (error) {
    is PedidoError.PedidoNotFound -> call.respond(
        HttpStatusCode.NotFound,
        buildErrorDto(error.message, HttpStatusCode.NotFound.value)
    )

    is PedidoError.InvalidPedidoId -> call.respond(
        HttpStatusCode.BadRequest,
        buildErrorDto(error.message, HttpStatusCode.BadRequest.value)
    )

    is PedidoError.PedidoSaveError -> call.respond(
        HttpStatusCode.NotFound,
        buildErrorDto(error.message, HttpStatusCode.NotFound.value)
    )

    is PedidoError.InvalidPedidoPage -> call.respond(
        HttpStatusCode.BadRequest,
        buildErrorDto(error.message, HttpStatusCode.BadRequest.value)
    )

    is PedidoError.InvalidPedidoFormat -> call.respond(
        HttpStatusCode.BadRequest,
        buildErrorDto(error.message, HttpStatusCode.BadRequest.value)
    )

    is PedidoError.MissingPedidoId -> call.respond(
        HttpStatusCode.BadRequest,
        buildErrorDto(error.message, HttpStatusCode.BadRequest.value)
    )

    is ApiError -> call.respond(
        HttpStatusCode.FailedDependency,
        buildErrorDto(error.message, HttpStatusCode.FailedDependency.value)
    )
}