package blanco.maldonado.mendoza.apiproductos

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@EnableR2dbcRepositories
@EnableCaching
class ApiProductosApplication

fun main(args: Array<String>) {
    runApplication<ApiProductosApplication>(*args)
}
