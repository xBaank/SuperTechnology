package pedidosApi.routing

import io.github.smiley4.ktorswaggerui.dsl.OpenApiRoute
import io.ktor.http.*
import pedidosApi.dto.requests.CreatePedidoDto
import pedidosApi.dto.requests.UpdatePedidoDto
import pedidosApi.dto.responses.ErrorDto
import pedidosApi.dto.responses.PagedFlowDto
import pedidosApi.dto.responses.PedidoDto


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
        headerParameter<String>("Authorization") {
            description = "Authorization header"
            required = true
        }
    }
    response {
        HttpStatusCode.OK to {
            description = "Paged result"
            body<PagedFlowDto<PedidoDto>>()
        }
        HttpStatusCode.Unauthorized to {
            description = "Unauthorized"
        }
    }
}


fun OpenApiRoute.getByUsuarioMe() {
    description = "Get pedidos paged by authenticated user"
    request {
        queryParameter<Int>("page") {
            description = "Page number, Default: 0"
            required = false
        }
        queryParameter<Int>("size") {
            description = "Page size, Default: 10, Max: 500"
            required = false
        }
        headerParameter<String>("Authorization") {
            description = "Authorization header"
            required = true
        }
    }
    response {
        HttpStatusCode.OK to {
            description = "Paged result"
            body<PagedFlowDto<PedidoDto>>()
        }
        HttpStatusCode.NotFound to {
            description = "User not found"
            body<ErrorDto>()
        }
        HttpStatusCode.Unauthorized to {
            description = "Unauthorized"
        }
    }
}


fun OpenApiRoute.getById() {
    description = "Get pedido by id"

    request {
        pathParameter<String>("id") {
            description = "pedido id"
            required = true
        }
        headerParameter<String>("Authorization") {
            description = "Authorization header"
            required = true
        }
    }
    response {
        HttpStatusCode.OK to {
            description = "Pedido"
            body<PedidoDto>()
        }
        HttpStatusCode.NotFound to {
            description = "Pedido not found"
            body<ErrorDto>()
        }
        HttpStatusCode.Unauthorized to {
            description = "Unauthorized"
        }
    }
}

fun OpenApiRoute.post() {
    description = "Updates or inserts pedidos"
    request {
        body<CreatePedidoDto>()
        headerParameter<String>("Authorization") {
            description = "Authorization header"
            required = true
        }
    }
    response {
        HttpStatusCode.Created to {
            description = "Pedido inserted"
            body<PedidoDto>()
        }
        HttpStatusCode.Unauthorized to {
            description = "Unauthorized"
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
        headerParameter<String>("Authorization") {
            description = "Authorization header"
            required = true
        }
        body<UpdatePedidoDto>()
    }
    response {
        HttpStatusCode.Created to {
            description = "Pedido saved"
            body<PedidoDto>()
        }
        HttpStatusCode.Unauthorized to {
            description = "Unauthorized"
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
        headerParameter<String>("Authorization") {
            description = "Authorization header"
            required = true
        }
    }
    response {
        HttpStatusCode.OK to {
            description = "Pedido deleted"
        }
        HttpStatusCode.Unauthorized to {
            description = "Unauthorized"
        }
    }
}