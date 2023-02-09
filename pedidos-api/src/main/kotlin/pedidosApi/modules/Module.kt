package pedidosApi.modules

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import pedidosApi.Config
import pedidosApi.fakes.fakeProductosClient
import pedidosApi.fakes.fakeUserClient
import pedidosApi.repositories.PedidosRepository

val mainModule = module {
    single { KMongo.createClient(Config.connectionString).coroutine.getDatabase(Config.database) }
    singleOf(::PedidosRepository)
    singleOf(::fakeUserClient)
    singleOf(::fakeProductosClient)
}



