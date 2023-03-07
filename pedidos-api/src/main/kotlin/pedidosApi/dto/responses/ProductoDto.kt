package pedidosApi.dto.responses

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class ProductoDto @OptIn(ExperimentalSerializationApi::class) constructor(
    @JsonNames("uuid")
    val id: String? = null,
    val nombre: String,
    val categoria: Categoria,
    val stock: Int,
    val description: String,
    val precio: Double,
    val activo: String,
    val createdAt: String? = null,
    val updateAt: String? = null,
    val deleteAt: String? = null
)

enum class Categoria {
    PIEZA, REPARACION, MONTAJE, PERSONALIZADO, MOVIL, PORTATIL, SOBREMESA, TABLET;
}