package pedidosApi.clients

import pedidosApi.dto.UsuarioDto
import retrofit2.http.*

interface UsuariosClient {
    @GET("/usuarios/{id}")
    suspend fun getUsuario(id: String): UsuarioDto?

    @GET("/usuarios")
    suspend fun getUsuarios(): List<UsuarioDto>

    @POST("/usuarios")
    suspend fun createUsuario(@Body usuario: UsuarioDto): UsuarioDto?

    @PUT("/usuarios")
    suspend fun updateUsuario(@Body usuario: UsuarioDto): UsuarioDto?

    @DELETE("/usuarios/{id}")
    suspend fun deleteUsuario(id: String)
}