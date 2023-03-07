package pedidosApi.extensions

import arrow.core.Either
import arrow.retrofit.adapter.either.networkhandling.CallError
import arrow.retrofit.adapter.either.networkhandling.HttpError
import arrow.retrofit.adapter.either.networkhandling.IOError
import arrow.retrofit.adapter.either.networkhandling.UnexpectedCallError
import pedidosApi.exceptions.ApiError

fun <T> Either<CallError, T>.mapToApiError(): Either<ApiError, T> = mapLeft {
    when (it) {
        is HttpError -> ApiError(it.message, it.code)
        is IOError -> ApiError(it.cause.message ?: "Unknown error", null)
        is UnexpectedCallError -> ApiError(it.cause.message ?: "Unknown error", null)
    }
}