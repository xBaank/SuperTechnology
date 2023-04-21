package pedidosApi

import arrow.retrofit.adapter.either.EitherCallAdapterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import pedidosApi.clients.ProductosClient
import pedidosApi.clients.UsuariosClient
import pedidosApi.models.Pedido
import pedidosApi.repositories.PedidosRepository
import retrofit2.Retrofit
import retrofit2.create

val retrofit = Retrofit.Builder()
    .addCallAdapterFactory(EitherCallAdapterFactory.create())
    .client(getUnsafeOkHttpClient())
    .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))

// used for testing
var module: Module? = null
val Application.mainModule: Module
    get() =
        module {
            val connectionString = environment.config.property("mongo.connectionString").getString()
            val database = environment.config.property("mongo.database").getString()
            val usuarioUrl = environment.config.property("usuarios.url").getString()
            val productosUrl = environment.config.property("productos.url").getString()
            single {
                KMongo.createClient(connectionString).coroutine.getDatabase(database).getCollection<Pedido>()
            }
            singleOf(::PedidosRepository)
            single { retrofit.baseUrl(usuarioUrl).build().create<UsuariosClient>() }
            single { retrofit.baseUrl(productosUrl).build().create<ProductosClient>() }
        }



