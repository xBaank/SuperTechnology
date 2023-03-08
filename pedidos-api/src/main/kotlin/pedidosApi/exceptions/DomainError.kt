package pedidosApi.exceptions

sealed class DomainError : Exception()
sealed class PedidoError : DomainError() {
    class PedidoNotFound(override val message: String) : PedidoError()
    class PedidoSaveError(override val message: String) : PedidoError()
    class InvalidPedidoId(override val message: String) : PedidoError()
    class InvalidPedidoPage(override val message: String) : PedidoError()
    class InvalidPedidoFormat(override val message: String) : PedidoError()
    class MissingPedidoId(override val message: String) : PedidoError()
    class InvalidPedido(override val message: String) : PedidoError()
}

class ApiError(override val message: String, val code: Int?) : DomainError()
