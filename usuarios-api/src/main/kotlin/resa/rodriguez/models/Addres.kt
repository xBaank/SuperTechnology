package resa.rodriguez.models

import java.util.*

data class Address (
    val id: UUID? = UUID.randomUUID(),
    val userId: UUID,
    val address: String
)
