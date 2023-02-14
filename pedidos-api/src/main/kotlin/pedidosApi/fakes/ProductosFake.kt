package pedidosApi.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.retrofit.adapter.either.networkhandling.CallError
import arrow.retrofit.adapter.either.networkhandling.HttpError
import pedidosApi.clients.ProductosClient
import pedidosApi.dto.ProductoDto

fun fakeProductosClient() = object : ProductosClient {
    val producto = ProductoDto(
        id = "fake",
        nombre = "Fake Producto",
        categoria = "Fake Categoria",
        stock = 10,
        descripcion = "Fake Descripcion",
        precio = 10.0,
        avatar = "Fake Avatar"
    )

    val productos = mutableMapOf(producto.id to producto)

    override suspend fun getProducto(id: String): Either<CallError, ProductoDto> {
        return productos[id]?.right() ?: HttpError(404, "Not found", "").left()
    }

    override suspend fun getProductos(): Either<CallError, List<ProductoDto>> {
        return productos.values.toList().right()
    }

}