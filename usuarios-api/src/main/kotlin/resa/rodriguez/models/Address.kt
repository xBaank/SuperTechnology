package resa.rodriguez.models

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
    @Column("user_id")
    val userId: UUID,
    val address: String
)