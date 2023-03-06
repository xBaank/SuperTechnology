package pedidosApi.clients

import arrow.core.Either
import arrow.retrofit.adapter.either.networkhandling.CallError
import pedidosApi.dto.responses.UsuarioDto
import retrofit2.http.*

interface UsuariosClient {
    @GET("/usuarios/id/{id}")
    suspend fun getUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Either<CallError, UsuarioDto>

    @GET("/usuarios/list")
    suspend fun getUsuarios(@Header("Authorization") token: String): Either<CallError, List<UsuarioDto>>
}