package pedidosApi.models

import org.litote.kmongo.Id
import org.litote.kmongo.newId
import pedidosApi.dto.ProductoDto
import pedidosApi.dto.UsuarioDto

data class Pedido(
    val id: Id<String> = newId(),
    val usuario: UsuarioDto,
    val productos: List<ProductoDto>,
    val total: Double
)