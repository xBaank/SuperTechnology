package pedidosApi.routing

import io.github.smiley4.ktorswaggerui.dsl.OpenApiRoute
import io.ktor.http.*
import pedidosApi.dto.responses.PagedFlowDto
import pedidosApi.models.Pedido

fun OpenApiRoute.getAll() {
    description = "Get pedidos paged"
    request {
        queryParameter<Int>("page") {
            description = "Page number, Default: 0"
            required = false
        }
        queryParameter<Int>("size") {
            description = "Page size, Default: 10, Max: 500"
            required = false
        }
    }
    response {
        HttpStatusCode.OK to {
            description = "Paged result"
            body<PagedFlowDto<Pedido>>()
        }
    }
}