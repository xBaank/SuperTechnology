package resa.rodriguez.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import resa.rodriguez.config.APIConfig

/**
 * Controlador rest especifico para comprobar que se puede conectar con la API en ejecucion con un cliente
 *
 */
@RestController
@RequestMapping(APIConfig.API_PATH + "/test")
class TestController {

    @GetMapping("")
    fun getAll(): String {
        return "Test"
    }
}