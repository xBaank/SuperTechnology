package pedidosApi.extensions

import io.ktor.server.application.*
import io.ktor.server.request.*

suspend inline fun <reified T : Any> ApplicationCall.receiveOrNull(): T? = try {
    receive()
}
catch (e: Exception) {
    println(e)
    null
}