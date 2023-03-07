package pedidosApi.clients

import arrow.core.Either
import arrow.retrofit.adapter.either.networkhandling.CallError
import pedidosApi.dto.responses.ProductoDto
import retrofit2.http.*

interface ProductosClient {
    @GET("/api/productos/{id}")
    suspend fun getProducto(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Either<CallError, ProductoDto>

    @GET("/api/productos")
    suspend fun getProductos(@Header("Authorization") token: String): Either<CallError, List<ProductoDto>>
}