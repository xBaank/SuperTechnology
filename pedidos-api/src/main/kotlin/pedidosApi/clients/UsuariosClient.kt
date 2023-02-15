package pedidosApi.clients

import arrow.core.Either
import arrow.retrofit.adapter.either.networkhandling.CallError
import pedidosApi.dto.responses.UsuarioDto
import retrofit2.http.*

interface UsuariosClient {
    @GET("/usuarios/{id}")
    suspend fun getUsuario(id: String): Either<CallError, UsuarioDto>

    @GET("/usuarios")
    suspend fun getUsuarios(): Either<CallError, List<UsuarioDto>>
}