package resa.rodriguez.controllers

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