package pedidosApi.clients

import arrow.core.Either
import arrow.retrofit.adapter.either.networkhandling.CallError
import pedidosApi.dto.ProductoDto
import retrofit2.http.*

interface ProductosClient {
    @GET("/productos/{id}")
    suspend fun getProducto(id: String): Either<CallError, ProductoDto>

    @GET("/productos")
    suspend fun getProductos(): Either<CallError, List<ProductoDto>>

    @POST("/productos")
    suspend fun createProducto(@Body producto: ProductoDto): Either<CallError, ProductoDto>

    @PUT("/productos")
    suspend fun updateProducto(@Body producto: ProductoDto): Either<CallError, ProductoDto>

    @DELETE("/productos/{id}")
    suspend fun deleteProducto(id: String): Either<CallError, Unit>
}