package pedidosApi

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.lang.System.getenv

object Config {
    //For some weird reason, the config file not updated even if we load it from the factory again.
    //The reason is that the config file is loaded only once, and then cached as a singleton.
    //So if we want to update the config file, we need to invalidate the cache.
    private var config: Config = ConfigFactory.load()
    val connectionString: String get() = getenv("MONGO_CONNECTION_STRING") ?: config.getString("mongo.connectionString")
    val database: String get() = getenv("MONGO_DATABASE") ?: config.getString("mongo.database")
    val usuariosUrl: String get() = getenv("USUARIOS_URL") ?: config.getString("usuarios.url")
    val productosUrl: String get() = getenv("PRODUCTOS_URL") ?: config.getString("productos.url")

    fun reload() {
        ConfigFactory.invalidateCaches()
        config = ConfigFactory.load()
    }
}