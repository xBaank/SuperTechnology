package pedidosApi.clients

import arrow.core.Either
import arrow.retrofit.adapter.either.networkhandling.CallError
import pedidosApi.dto.UsuarioDto
import retrofit2.http.*

interface UsuariosClient {
    @GET("/usuarios/{id}")
    suspend fun getUsuario(id: String): Either<CallError, UsuarioDto>

    @GET("/usuarios")
    suspend fun getUsuarios(): Either<CallError, List<UsuarioDto>>

    @POST("/usuarios")
    suspend fun createUsuario(@Body usuario: UsuarioDto): Either<CallError, UsuarioDto>

    @PUT("/usuarios")
    suspend fun updateUsuario(@Body usuario: UsuarioDto): Either<CallError, UsuarioDto>

    @DELETE("/usuarios/{id}")
    suspend fun deleteUsuario(id: String): Either<CallError, Unit>
}