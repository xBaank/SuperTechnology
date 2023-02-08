package productosApi.model

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
    ){
    enum class Categoria(categoria: String){
        PIEZA("PIEZA"),
        REPARACION("REPARACION"),
        MONTAJE("MONTAJE"),
        PERSONALIZADO("PERSONALIZADO"),
        MOVIL("MOVIL"),
        PORTATIL("PORTATIL"),
        SOBREMESA("SOBREMESA"),
        TABLET("TABLET");

        companion object {
            fun from(categoria: String): Categoria{
                return when ( categoria.uppercase()){
                    "PIEZA" -> PIEZA
                    "REPARACION" -> REPARACION
                    "MONTAJE" -> MONTAJE
                    "PERSONALIZADO" -> PERSONALIZADO
                    "MOVIL" -> MOVIL
                    "PORTATIL" -> PORTATIL
                    "SOBREMESA" -> SOBREMESA
                    "TABLET" -> TABLET
                    else -> throw IllegalArgumentException("Categorías no válidas")
                }
            }
        }
    }
}