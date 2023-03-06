package integration.data

import integration.data.fakes.fakeProductosClient
import integration.data.fakes.fakeUserClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import pedidosApi.models.Pedido
import pedidosApi.repositories.PedidosRepository

val testModule: Module
    get() = module {
        val connectionString = PedidosData.config.getString("mongo.connectionString")
        val database = PedidosData.config.getString("mongo.database")
        single {
            KMongo.createClient(connectionString).coroutine.getDatabase(database).getCollection<Pedido>()
        }
        singleOf(::PedidosRepository)
        singleOf(::fakeUserClient)
        singleOf(::fakeProductosClient)
    }



