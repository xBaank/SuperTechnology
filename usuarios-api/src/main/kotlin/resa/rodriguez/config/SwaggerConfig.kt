package resa.rodriguez.config

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for swagger.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
@Configuration
class SwaggerConfig {
    @Bean
    fun apiInfo(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("SuperTechnology - Usuarios")
                    .version(APIConfig.API_VERSION)
                    .description(
                        "Micro servicio de gestión de usuarios de una tienda de tecnología para las asignaturas " +
                                "de Acceso a Datos y Programación de Procesos y Servicios del IES Luis Vives (Leganés) " +
                                "curso 22/23."
                    )
            )
            .externalDocs(
                ExternalDocumentation()
                    .description("Repositorio del proyecto en Github")
                    .url("https://github.com/xBaank/SuperTechnology/tree/master/usuarios-api")
            )
    }

    @Bean
    fun httpApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("http")
            .pathsToMatch("/usuarios/**")
            .displayName("HTTP-API SuperTechnology - Usuarios")
            .build()
    }
}