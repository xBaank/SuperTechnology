package integration.data.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.retrofit.adapter.either.networkhandling.CallError
import arrow.retrofit.adapter.either.networkhandling.HttpError
import pedidosApi.clients.UsuariosClient
import pedidosApi.dto.responses.Role
import pedidosApi.dto.responses.UsuarioDto
import retrofit2.http.Header
import retrofit2.http.Path

//pa testear por ahora
fun fakeUserClient() = object : UsuariosClient {
    val user = UsuarioDto(
        username = "Fake User",
        email = "test@gmail.com",
        role = Role.USER,
        addresses = emptySet(),
        avatar = "",
        createdAt = "2021-01-01",
        active = true
    )
    val users = mutableMapOf(user.username to user)

    override suspend fun getUsuario(
        @Header(value = "Authorization") token: String,
        @Path(value = "username") username: String
    ): Either<CallError, UsuarioDto> {
        return users[username]?.right() ?: HttpError(404, "User not found", "").left()
    }

    override suspend fun getUsuarios(@Header(value = "Authorization") token: String): Either<CallError, List<UsuarioDto>> {
        return users.values.toList().right()
    }

}