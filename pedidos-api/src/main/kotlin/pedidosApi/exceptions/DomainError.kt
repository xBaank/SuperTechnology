package pedidosApi.exceptions

sealed interface DomainError
sealed interface PedidoError : DomainError {
    class PedidoNotFound(val message: String) : PedidoError
    class PedidoSaveError(val message: String) : PedidoError
    class InvalidPedidoId(val message: String) : PedidoError
    class InvalidPedidoPage(val message: String) : PedidoError
    class InvalidPedidoFormat(val message: String) : PedidoError
    class MissingPedidoId(val message: String) : PedidoError
}

class ApiError(val message: String) : DomainError
