package pedidosApi.fakes

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

    override suspend fun getProducto(id: String): ProductoDto? = producto

    override suspend fun getProductos(): List<ProductoDto> = listOf(producto)

    override suspend fun createProducto(producto: ProductoDto): ProductoDto? = producto

    override suspend fun updateProducto(producto: ProductoDto): ProductoDto? = producto

    override suspend fun deleteProducto(id: String) = Unit

}