package integration

import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

suspend inline fun <reified T> HttpResponse.bodyAs() = Json.decodeFromString<T>(bodyAsText())
