package resa.rodriguez.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import resa.rodriguez.config.APIConfig
import resa.rodriguez.exceptions.StorageException
import resa.rodriguez.exceptions.StorageExceptionBadRequest
import resa.rodriguez.services.storage.IStorageService
import java.io.IOException
import java.time.LocalDateTime

/**
 * Controlador encargado del almacenamiento, haciendo uso del servicio especifico
 *
 * @property storageService
 */
@RestController
@RequestMapping("${APIConfig.API_PATH}/storage")
class StorageController
@Autowired constructor(
    private val storageService: IStorageService
) {
    @Operation(summary = "Serve File", description = "Metodo que devuelve el recurso cuyo nombre fue pasado por parametro.", tags = ["STORAGE"])
    @Parameter(name = "Filename", description = "Nombre del archivo a buscar.", required = true)
    @Parameter(name = "Request", description = "Peticion http.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el archivo como cuerpo y un content type con el tipo de recurso que es.")
    @ApiResponse(responseCode = "400", description = "Cuando no puede resolver el tipo de archivo que es.")
    @ApiResponse(responseCode = "404", description = "Cuando no encuentra el archivo o no lo puede leer.")
    @GetMapping(value = ["{filename:.+}"])
    @ResponseBody
    fun serveFile(@PathVariable filename: String?, request: HttpServletRequest): ResponseEntity<Resource> = runBlocking {
        val file: Resource = CoroutineScope(Dispatchers.IO).async { storageService.loadAsResource(filename.toString()) }.await()
        var contentType: String?
        contentType = try {
            request.servletContext.getMimeType(file.file.absolutePath)
        } catch (ex: IOException) {
            throw StorageExceptionBadRequest("Unable to guess file type.", ex)
        }
        if (contentType == null) contentType = "application/octet-stream"
        ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(file)
    }

    @Operation(summary = "Upload File", description = "Metodo que guarda el recurso cuyo nombre fue pasado por parametro y devuelve un mapa con sus caracteristicas.", tags = ["STORAGE"])
    @Parameter(name = "File", description = "Archivo multiparte a guardar.", required = true)
    @ApiResponse(responseCode = "201", description = "Response Entity con la url del recurso guardado, su nombre y su fecha de creacion.")
    @ApiResponse(responseCode = "400", description = "Cuando no puede guardar el archivo.")
    @PostMapping(value = [""], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(@RequestPart("file") file: MultipartFile): ResponseEntity<Map<String, String>> = runBlocking {
        try {
            if (!file.isEmpty) {
                val fileStored = CoroutineScope(Dispatchers.IO).async { storageService.store(file) }.await()
                val url = storageService.getUrl(fileStored)
                val response = mapOf("url" to url, "name" to fileStored, "created_at" to LocalDateTime.now().toString())
                ResponseEntity.status(HttpStatus.CREATED).body(response)
            }
            else throw StorageExceptionBadRequest("You cannot upload an empty file.")
        } catch (e: StorageException) {
            throw StorageExceptionBadRequest(e.message.toString())
        }
    }

    @Operation(summary = "Delete File", description = "Metodo que borra un recurso cuyo nombre fue pasado por parametro y devuelve el recurso borrado.", tags = ["STORAGE"])
    @Parameter(name = "Filename", description = "Nombre del archivo a buscar.", required = false)
    @Parameter(name = "Request", description = "Peticion http.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity con el recurso borrado.")
    @ApiResponse(responseCode = "400", description = "Cuando no puede borrar el archivo.")
    @DeleteMapping(value = ["{filename:.+}"])
    @ResponseBody
    fun deleteFile(@PathVariable filename: String?, request: HttpServletRequest): ResponseEntity<Resource> = runBlocking {
        try {
            CoroutineScope(Dispatchers.IO).launch { storageService.delete(filename.toString()) }.join()
            ResponseEntity.ok().build()
        } catch (e: StorageException) {
            throw StorageExceptionBadRequest(e.message.toString())
        }
    }
}