package pedidosApi.extensions

import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*

suspend inline fun <reified T : Any> ApplicationCall.receiveOrNull(): T? = try {
    receive()
} catch (e: BadRequestException) {
    if (e.cause is ContentConvertException) null
    else throw e
}