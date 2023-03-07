package resa.rodriguez

import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import resa.rodriguez.controllers.UserController
import resa.rodriguez.db.getUsersInit

@SpringBootApplication
@EnableR2dbcRepositories
@EnableCaching
class UsuariosApiApplication
@Autowired constructor(
	private val controller: UserController
) : CommandLineRunner {
	override fun run(vararg args: String?): Unit = runBlocking {
		// Initial data
		getUsersInit().forEach {
			controller.createByAdminInitializer(it)
		}
	}
}

fun main(args: Array<String>) {
    runApplication<UsuariosApiApplication>(*args)
}