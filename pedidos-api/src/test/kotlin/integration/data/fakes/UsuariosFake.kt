package integration.data.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.retrofit.adapter.either.networkhandling.CallError
import arrow.retrofit.adapter.either.networkhandling.HttpError
import pedidosApi.clients.UsuariosClient
import pedidosApi.dto.responses.Role
import pedidosApi.dto.responses.UsuarioDto

//pa testear por ahora
fun fakeUserClient() = object : UsuariosClient {
    val user = UsuarioDto(
        id = "fake",
        username = "Fake User",
        email = "test@gmail.com",
        role = Role.USER
    )
    val users = mutableMapOf(user.id to user)

    override suspend fun getUsuario(id: String): Either<CallError, UsuarioDto> {
        return users[id]?.right() ?: HttpError(404, "User not found", "").left()
    }

    override suspend fun getUsuarios(): Either<CallError, List<UsuarioDto>> {
        return users.values.toList().right()
    }

}