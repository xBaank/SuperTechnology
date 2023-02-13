package resa.rodriguez.models

import jakarta.validation.constraints.NotEmpty
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

/**
 * Modelo para direcciones
 *
 * @property id Identificador principal, UUID
 * @property userId Identificador del usuario asociado, UUID
 * @property address Nombre de la direccion, String
 */
@Table(name = "addresses")
data class Address(
    @Id
    val id: UUID? = null,
    @NotEmpty(message = "Debe haber un id de usuario.")
    @Column("user_id")
    val userId: UUID,
    @NotEmpty(message = "Debe haber una direccion asociada.")
    val address: String
)
