package pedidosApi

import com.typesafe.config.ConfigFactory
import java.lang.System.*

object Config {
    val config = ConfigFactory.load()
    val connectionString = getenv("mongoConnectionString") ?: config.getString("mongo.connectionString")
    val database = getenv("mongoDatabase") ?: config.getString("mongo.database")
}