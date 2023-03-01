package pedidosApi.modules

import arrow.retrofit.adapter.either.EitherCallAdapterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.ktor.server.application.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import pedidosApi.fakes.fakeProductosClient
import pedidosApi.fakes.fakeUserClient
import pedidosApi.models.Pedido
import pedidosApi.repositories.PedidosRepository
import retrofit2.Retrofit

@OptIn(ExperimentalSerializationApi::class)
val retrofit = Retrofit.Builder()
    .addCallAdapterFactory(EitherCallAdapterFactory.create())
    .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))


val Application.mainModule
    get() = module {
        single {
            KMongo.createClient(environment.config.property("mongo.connectionString").getString())
                .getDatabase(environment.config.property("mongo.database").getString())
                .coroutine
                .getCollection<Pedido>()
        }
        singleOf(::PedidosRepository)
        singleOf(::fakeUserClient)
        singleOf(::fakeProductosClient)
        /*    single { retrofit.baseUrl(Config.usuariosUrl).build().create<UsuariosClient>() }
            single { retrofit.baseUrl(Config.productosUrl).build().create<ProductosClient>() }*/
    }



