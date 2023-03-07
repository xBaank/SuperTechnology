package integration.data.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.retrofit.adapter.either.networkhandling.CallError
import arrow.retrofit.adapter.either.networkhandling.HttpError
import pedidosApi.clients.ProductosClient
import pedidosApi.dto.responses.Categoria
import pedidosApi.dto.responses.ProductoDto
import retrofit2.http.Header
import retrofit2.http.Path

fun fakeProductosClient() = object : ProductosClient {
    val producto = ProductoDto(
        id = "fake",
        nombre = "Fake Producto",
        categoria = Categoria.MONTAJE,
        stock = 10,
        description = "Fake Descripcion",
        precio = 10.0,
        activo = "true",
        createdAt = "2021-01-01",
        updateAt = "2021-01-01",
        deleteAt = "2021-01-01"
    )

    val productos = mutableMapOf(producto.id to producto)

    override suspend fun getProducto(
        @Header(value = "Authorization") token: String,
        @Path(value = "id") id: String
    ): Either<CallError, ProductoDto> {
        return productos[id]?.right() ?: HttpError(404, "Not found", "").left()
    }

    override suspend fun getProductos(@Header(value = "Authorization") token: String): Either<CallError, List<ProductoDto>> {
        return productos.values.toList().right()
    }

    override suspend fun updateProducto(
        token: String,
        id: String,
        producto: ProductoDto
    ): Either<CallError, ProductoDto> {
        productos[id] = producto
        return producto.right()
    }

}