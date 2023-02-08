package pedidosApi.clients

import pedidosApi.dto.ProductoDto
import retrofit2.http.*

interface ProductosClient {
    @GET("/productos/{id}")
    suspend fun getProducto(id: String): ProductoDto?

    @GET("/productos")
    suspend fun getProductos(): List<ProductoDto>

    @POST("/productos")
    suspend fun createProducto(@Body producto: ProductoDto): ProductoDto?

    @PUT("/productos")
    suspend fun updateProducto(@Body producto: ProductoDto): ProductoDto?

    @DELETE("/productos/{id}")
    suspend fun deleteProducto(id: String)
}