package resa.rodriguez.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

/**
 * Clase que se encarga de conectar con la base de datos y cargar el schema generado en resources.
 * Esta accion es necesaria por r2dbc.
 * Cuenta con las anotaciones necesarias para que Spring lo detecte.
 */
@Configuration
class LoadSchema {

    @Bean
    fun initializer(@Qualifier("connectionFactory") connectionFactory: ConnectionFactory?): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        if (connectionFactory != null) {
            initializer.setConnectionFactory(connectionFactory)
        }
        val resource = ResourceDatabasePopulator(ClassPathResource("schema.sql"))
        initializer.setDatabasePopulator(resource)
        return initializer
    }
}