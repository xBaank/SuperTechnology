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
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import resa.rodriguez.config.APIConfig
import resa.rodriguez.exceptions.StorageExceptionNotFound
import resa.rodriguez.exceptions.StorageException
import resa.rodriguez.exceptions.StorageExceptionBadRequest
import resa.rodriguez.models.User
import resa.rodriguez.services.storage.IStorageService
import java.io.IOException
import java.time.LocalDateTime

/**
 * Controlador encargado del almacenamiento, haciendo uso del servicio especifico
 *
 * @property storageService
 */
/**
 * Controller that will manage every endpoint related to the storage system
 * by calling the storage service.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @property storageService
 */
@RestController
@RequestMapping("${APIConfig.API_PATH}/storage")
class StorageController
@Autowired constructor(
    private val storageService: IStorageService
) {
    /**
     * Endpoint for finding a resource with the given filename.
     * It will return a response entity with a resource whose name corresponds to the filename parameter.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param filename Filename to be searched.
     * @param request Http request.
     * @return Response Entity with a resource whose name corresponds to the filename parameter.
     * @throws StorageExceptionBadRequest When the type of file cannot be determined.
     * @throws StorageExceptionNotFound When a file is not found or cannot be read.
     */
    @Operation(summary = "Serve File", description = "Endpoint for finding a resource with the given filename.", tags = ["STORAGE"])
    @Parameter(name = "Filename", description = "Filename to be searched.", required = true)
    @Parameter(name = "Request", description = "Http request.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with a resource whose name corresponds to the filename parameter.")
    @ApiResponse(responseCode = "400", description = "When the type of file cannot be determined.")
    @ApiResponse(responseCode = "404", description = "When a file is not found or cannot be read.")
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

    /**
     * Endpoint for uploading a multipart file.
     * It will return a response entity with a map of string to string with
     * the url, name and creation date of the saved file.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param file Multipart file to be saved.
     * @return Response Entity with a map of string to string with
     * the url, name and creation date of the saved file.
     * @throws StorageExceptionBadRequest When the file could not be saved.
     */
    @Operation(summary = "Upload File", description = "Endpoint for uploading a multipart file.", tags = ["STORAGE"])
    @Parameter(name = "File", description = "Multipart file to be saved.", required = true)
    @ApiResponse(responseCode = "201", description = "Response Entity with a map of string to string with the url, name and creation date of the saved file.")
    @ApiResponse(responseCode = "400", description = "When the file could not be saved.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping(value = [""], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(@RequestPart("file") file: MultipartFile, @AuthenticationPrincipal user: User): ResponseEntity<Map<String, String>> = runBlocking {
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

    /**
     * Endpoint for deleting a resource whose name matches the one given.
     * It will return a response entity with the deleted resource.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param filename Filename of the file to be deleted.
     * @param request Http request.
     * @return Response Entity with the deleted resource.
     * @throws StorageExceptionBadRequest When the file could not be deleted.
     */
    @Operation(summary = "Delete File", description = "Endpoint for deleting a resource whose name matches the one given.", tags = ["STORAGE"])
    @Parameter(name = "Filename", description = "Filename of the file to be deleted.", required = false)
    @Parameter(name = "Request", description = "Http request.", required = true)
    @ApiResponse(responseCode = "200", description = "Response Entity with the deleted resource.")
    @ApiResponse(responseCode = "400", description = "When the file could not be deleted.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping(value = ["{filename:.+}"])
    @ResponseBody
    fun deleteFile(@PathVariable filename: String?, request: HttpServletRequest, @AuthenticationPrincipal user: User): ResponseEntity<Resource> = runBlocking {
        try {
            CoroutineScope(Dispatchers.IO).launch { storageService.delete(filename.toString()) }.join()
            ResponseEntity.ok().build()
        } catch (e: StorageException) {
            throw StorageExceptionBadRequest(e.message.toString())
        }
    }
}