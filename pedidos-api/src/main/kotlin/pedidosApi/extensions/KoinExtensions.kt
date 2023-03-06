package pedidosApi.extensions

import org.koin.core.context.GlobalContext

inline fun <reified T : Any> inject() = lazy { GlobalContext.get().get<T>() }