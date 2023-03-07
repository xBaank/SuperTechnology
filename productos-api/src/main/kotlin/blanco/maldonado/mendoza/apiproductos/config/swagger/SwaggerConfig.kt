package blanco.maldonado.mendoza.apiproductos.config.swagger

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
//http://localhost:8080/swagger-ui/index.html
@Configuration
class SwaggerConfig {
    @Bean
    fun apiInfo(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("API REST SuperTechnology Spring Boot Reactive")
                    .version("1.0.0")
                    .description("API de productos para el curso de dam en el que se desarrolla una API REST con Spring Bot del curso Desarrollo de un API REST con Spring Boot")
                    .termsOfService("")
                    .license(
                        License()
                            .name("CC BY-NC-SA 4.0")
                            .url("")
                    )
                    .contact(
                        Contact()
                            .name("Alfredo, Sebastian, Azahara")
                            .email("")
                            .url("")
                    )

            )
            .externalDocs(
                ExternalDocumentation()
                    .description("Repositorio y Documentación del Proyecto y API")
                    .url("https://github.com/xBaank/SuperTechnology")
            )
    }


    @Bean
    fun httpApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("http")
            //.pathsToMatch("/api/**")
            //.pathsToMatch("/api/tenistas/**")
            .pathsToMatch("/api/productos/**")
            .displayName("HTTP-API Productos")
            .build()
    }
}