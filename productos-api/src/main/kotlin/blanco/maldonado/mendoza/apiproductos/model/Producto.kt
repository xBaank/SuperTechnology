package blanco.maldonado.mendoza.apiproductos.model

/**
 * @since 16/02/2023
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table(name = "productos")
data class Producto(
    @Id
    val id: Long? = null,
    val uuid: String? = null,
    val nombre: String,
    val categoria: Categoria,
    val stock: Int,
    val description: String,
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column("updated_at")
    val updateAt: LocalDateTime? = null,
    @Column("deleted_at")
    var deleteAt: LocalDateTime? = null,
    val precio: Double,
    val activo: Boolean
) {
    enum class Categoria {
        PIEZA, REPARACION, MONTAJE, PERSONALIZADO, MOVIL, PORTATIL, SOBREMESA, TABLET;
    }
}