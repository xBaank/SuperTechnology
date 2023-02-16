package pedidosApi.routing

import io.github.smiley4.ktorswaggerui.dsl.OpenApiRoute
import io.ktor.http.*
import pedidosApi.dto.requests.CreatePedidoDto
import pedidosApi.dto.responses.PagedFlowDto
import pedidosApi.dto.responses.PedidoDto
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

fun OpenApiRoute.post() {
    description = "Updates or inserts pedidos"
    request {
        queryParameter<CreatePedidoDto>("pedido") {
            description = "CreatePedidoDto that will be inserted"
            required = true
        }
    }
    response {
        HttpStatusCode.Created to {
            description = "Pedido inserted"
            body<PedidoDto>()
        }
    }
}

fun OpenApiRoute.put() {
    description = "Inserts or updates pedidos with given id"
    request {
        queryParameter<String>("id") {
            description = "Id from the pedido"
            required = true
        }
    }
    response {
        HttpStatusCode.Created to {
            description = "Pedido saved"
            body<PedidoDto>()
        }
    }
}

fun OpenApiRoute.delete() {
    description = "Deletes pedidos with given id"
    request {
        queryParameter<String>("id") {
            description = "Id from the pedido"
            required = true
        }
    }
    response {
        HttpStatusCode.OK to {
            description = "Pedido deleted"
            body<Unit>()
        }
    }
}