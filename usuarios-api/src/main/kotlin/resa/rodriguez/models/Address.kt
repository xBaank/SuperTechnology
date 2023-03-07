package resa.rodriguez.models

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

/**
 * Model for addresses.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @property id Main identifier, UUID.
 * @property userId Associated user's id, UUID.
 * @property address Address name, String.
 */
@Table(name = "addresses")
data class Address(
    @Id
    val id: UUID? = null,
    @Column("user_id")
    val userId: UUID,
    val address: String
)