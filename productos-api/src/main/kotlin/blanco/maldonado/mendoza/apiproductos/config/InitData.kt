/**
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @since 6/3/2023
 */

package blanco.maldonado.mendoza.apiproductos.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

/**
 * Init data
 *
 */
@Configuration
class InitData {

    /**
     * Initializer: Initializer the schema.sql
     *
     * @param connectionFactory
     * @return initializer
     */
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