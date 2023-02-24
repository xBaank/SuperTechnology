package blanco.maldonado.mendoza.apiproductos.model

/**
 * @since 16/02/2023
 * @author Azahara Blanco, Alfredo Maldonado, Sebastian Mendoza
 */
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
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
    @NotEmpty(message = "El nombre no puede estar vacío")
    var nombre: String,
    @NotEmpty(message = "La categoría no puede estar vacía")
    val categoria: Categoria,
    @Min(value = 0, message = "El stick no puede ser negativo")
    val stock: Int,
    @NotEmpty(message = "La descripción no puede estar vacía")
    val description: String,
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column("updated_at")
    val updateAt: LocalDateTime? = null,
    @Column("deleted_at")
    val deleteAt: LocalDateTime? = null,
    @Min(value = 0, message = "El precio no puede ser negativo")
    val precio: Double,
    val activo: Boolean
) {
    enum class Categoria {
        PIEZA, REPARACION, MONTAJE, PERSONALIZADO, MOVIL, PORTATIL, SOBREMESA, TABLET;
    }
}