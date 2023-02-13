package pedidosApi.modules

import arrow.retrofit.adapter.either.EitherCallAdapterFactory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import pedidosApi.Config
import pedidosApi.fakes.fakeProductosClient
import pedidosApi.fakes.fakeUserClient
import pedidosApi.repositories.PedidosRepository
import retrofit2.Retrofit

val mainModule = module {
    single { KMongo.createClient(Config.connectionString).coroutine.getDatabase(Config.database) }
    singleOf(::PedidosRepository)
    singleOf(::fakeUserClient)
    singleOf(::fakeProductosClient)
    single {
        Retrofit.Builder()
            .addCallAdapterFactory(EitherCallAdapterFactory.create())
    }
}



