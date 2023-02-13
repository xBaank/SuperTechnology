package pedidosApi.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.retrofit.adapter.either.networkhandling.CallError
import arrow.retrofit.adapter.either.networkhandling.HttpError
import pedidosApi.clients.UsuariosClient
import pedidosApi.dto.UsuarioDto

//pa testear por ahora
fun fakeUserClient() = object : UsuariosClient {
    val user = UsuarioDto(
        id = "fake",
        username = "Fake User",
        email = "test@gmail.com"
    )
    val users = mutableMapOf<String, UsuarioDto>()

    override suspend fun getUsuario(id: String): Either<CallError, UsuarioDto> {
        return users[id]?.right() ?: HttpError(404, "Not found", "").left()
    }

    override suspend fun getUsuarios(): Either<CallError, List<UsuarioDto>> {
        return users.values.toList().right()
    }

    override suspend fun createUsuario(usuario: UsuarioDto): Either<CallError, UsuarioDto> {
        users[usuario.id] = usuario
        return usuario.right()
    }

    override suspend fun updateUsuario(usuario: UsuarioDto): Either<CallError, UsuarioDto> {
        users[usuario.id] = usuario
        return usuario.right()
    }

    override suspend fun deleteUsuario(id: String): Either<CallError, Unit> {
        return Unit.right()
    }

}