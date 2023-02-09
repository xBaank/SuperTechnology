package resa.rodriguez.models

import jakarta.validation.constraints.NotEmpty
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "addresses")
data class Address (
    @Id
    val id: UUID? = UUID.randomUUID(),
    @NotEmpty(message = "Debe haber un id de usuario.")
    @Column("user_id")
    val userId: UUID,
    @NotEmpty(message = "Debe haber una direccion asociada.")
    val address: String
)
