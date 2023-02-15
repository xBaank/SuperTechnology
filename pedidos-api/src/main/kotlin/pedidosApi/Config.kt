package pedidosApi

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.lang.System.getenv

object Config {
    var config: Config = ConfigFactory.load()
    val connectionString get() = getenv("MONGO_CONNECTION_STRING") ?: config.getString("mongo.connectionString")
    val database get() = getenv("MONGO_DATABASE") ?: config.getString("mongo.database")
    val usuariosUrl get() = getenv("USUARIOS_URL") ?: config.getString("usuarios.url")
    val productosUrl get() = getenv("PRODUCTOS_URL") ?: config.getString("productos.url")
}