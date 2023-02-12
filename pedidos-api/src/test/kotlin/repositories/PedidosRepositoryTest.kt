package repositories

import org.junit.jupiter.api.TestInstance
import org.litote.kmongo.newId
import pedidosApi.dto.CreatePedidoDto
import pedidosApi.dto.ProductoDto
import pedidosApi.dto.TareaDto
import pedidosApi.dto.UsuarioDto
import pedidosApi.models.Tarea
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PedidosRepositoryTest {

    private val usuario = UsuarioDto(UUID.randomUUID().toString(), "Nombre", "correo@email.com")

    private val producto = ProductoDto(
        UUID.randomUUID().toString(),
        "NombreProd", "categoriaProd", 5, "descrProd", 12.2, ""
    )

    private val tarea = TareaDto(
        id = newId<Tarea>().toString(),
        productos = listOf(producto),
        empleado = UsuarioDto(UUID.randomUUID().toString(), "empleadoUsername", "emp@email.com")
    )

    private val pedido = CreatePedidoDto(
        //id = newId<Pedido>().toString(),
        usuario = usuario.username,
        productos = listOf(producto.toString())
    )
}