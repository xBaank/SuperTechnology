package pedidosApi.fakes

import pedidosApi.clients.UsuariosClient
import pedidosApi.dto.UsuarioDto

//pa testear por ahora
fun fakeUserClient() = object : UsuariosClient {
    val user = UsuarioDto(
        id = "fake",
        username = "Fake User",
        email = "test@gmail.com"
    )

    override suspend fun getUsuario(id: String): UsuarioDto = user

    override suspend fun getUsuarios(): List<UsuarioDto> = listOf(user)

    override suspend fun createUsuario(usuario: UsuarioDto): UsuarioDto = user

    override suspend fun updateUsuario(usuario: UsuarioDto): UsuarioDto = user

    override suspend fun deleteUsuario(id: String) = Unit
}