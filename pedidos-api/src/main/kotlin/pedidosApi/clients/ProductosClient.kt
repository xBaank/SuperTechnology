package pedidosApi.clients

import arrow.core.Either
import arrow.retrofit.adapter.either.networkhandling.CallError
import pedidosApi.dto.responses.ProductoDto
import retrofit2.http.*

interface ProductosClient {
    @GET("/api/products/admin/{id}")
    suspend fun getProducto(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Either<CallError, ProductoDto>

    @GET("/api/products")
    suspend fun getProductos(@Header("Authorization") token: String): Either<CallError, List<ProductoDto>>

    @PUT("/api/products/{id}")
    suspend fun updateProducto(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body producto: ProductoDto
    ): Either<CallError, ProductoDto>
}