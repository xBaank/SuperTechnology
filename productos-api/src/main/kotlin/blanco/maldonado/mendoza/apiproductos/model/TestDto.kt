package blanco.maldonado.mendoza.apiproductos.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDateTime

data class TestDto(
    // Podemos validar los campos con anotaciones de Spring
    @NotEmpty(message = "El mensaje no puede estar vacío")
    val message: String,
    @field:NotBlank(message = "La fecha no puede estar vacía")
    val createdAt: String = LocalDateTime.now().toString()
)