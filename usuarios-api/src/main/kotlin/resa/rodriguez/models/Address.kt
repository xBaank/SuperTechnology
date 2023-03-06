package resa.rodriguez.models

import jakarta.validation.constraints.NotEmpty
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

/**
 * Model for addresses.
 * @author Mario Gonzalez, Daniel Rodriguez, Joan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @property id Main identifier, UUID.
 * @property userId Associated user's id, UUID.
 * @property address Address name, String.
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