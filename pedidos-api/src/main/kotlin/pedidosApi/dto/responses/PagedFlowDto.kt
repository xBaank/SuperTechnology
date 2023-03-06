package pedidosApi.dto.responses

import kotlinx.serialization.Serializable

@Serializable
data class PagedFlowDto<T>(
    val page: Int,
    val size: Long,
    val result: List<T>
)