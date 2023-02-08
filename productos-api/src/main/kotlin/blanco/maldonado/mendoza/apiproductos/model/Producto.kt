package blanco.maldonado.mendoza.apiproductos.model

import java.time.LocalDateTime
import java.util.*

data class Producto(
    val id: UUID = UUID.randomUUID(),
    val nombre: String,
    val categoria: Categoria,
    val stock: Int,
    val description: String,
    val createdAt: LocalDateTime,
    val updateAt: LocalDateTime,
    val deleteAt: LocalDateTime,
    val precio: Double,
    val activo: Boolean
) {
    enum class Categoria {
        PIEZA, REPARACION, MONTAJE, PERSONALIZADO, MOVIL, PORTATIL, SOBREMESA, TABLET;
    }
}