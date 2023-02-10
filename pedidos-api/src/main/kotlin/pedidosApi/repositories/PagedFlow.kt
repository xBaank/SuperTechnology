package pedidosApi.repositories

import kotlinx.coroutines.flow.Flow


class PagedFlow<T>(val page: Int, val size: Int, results: Flow<T>) : Flow<T> by results