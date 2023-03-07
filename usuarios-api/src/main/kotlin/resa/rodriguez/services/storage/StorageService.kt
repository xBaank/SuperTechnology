package resa.rodriguez.services.storage

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import resa.rodriguez.controllers.StorageController
import resa.rodriguez.exceptions.AddressExceptionNotFound
import resa.rodriguez.exceptions.StorageExceptionBadRequest
import resa.rodriguez.exceptions.StorageExceptionNotFound
import resa.rodriguez.exceptions.UserExceptionNotFound
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.stream.Stream

private val log = KotlinLogging.logger {}

/**
 * Service that will execute the corresponding queries using the repositories and then return the correct DTOs.
 * @param path Path to the storage folder
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
@Service
class StorageService(
    @Value("\${upload.root-location}") path: String,
) : IStorageService {
    private val rootLocation: Path = Paths.get(path)

    init {
        initStorageDirectory()
    }

    /**
     * Function that will initialize the Storage Service by creating a folder on which to save the images,
     * if it has not already been created.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     */
    final override fun initStorageDirectory() {
        if (!Files.exists(rootLocation)) {
            log.info { "Creando directorio de almacenamiento: $rootLocation" }
            Files.createDirectory(rootLocation)
        } else {
            log.debug { "El directorio existe; eliminacion de datos en proceso..." }
            deleteAll()
            // We create the directory again once the data has been deleted, including the directory itself.
            Files.createDirectory(rootLocation)
        }
    }

    /**
     * Function that will save a multipart file into the storage folder.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param file Multipart file to be saved.
     * @return Name of the stored file.
     * @throws StorageExceptionBadRequest when it cannot save the file.
     */
    override fun store(file: MultipartFile): String {
        val fileName = StringUtils.cleanPath(file.originalFilename.toString())
        val extension = StringUtils.getFilenameExtension(fileName).toString()
        val name = fileName.replace(".$extension", "")

        val storedName = "${name}_${System.currentTimeMillis()}.$extension"
        try {
            if (file.isEmpty)
                throw StorageExceptionBadRequest("Could not store $fileName. File is empty.")
            if (fileName.contains(".."))
                throw StorageExceptionBadRequest("You cannot store $fileName in a parent directory.")
            file.inputStream.use { input ->
                Files.copy(
                    input, rootLocation.resolve(storedName),
                    StandardCopyOption.REPLACE_EXISTING
                )
                return storedName
            }
        } catch (e: IOException) {
            throw StorageExceptionBadRequest("Could not store $fileName", e)
        }
    }

    /**
     * Function that will save a multipart file with the given filename into the storage folder.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param file Multipart file to be saved.
     * @param fileName Name that the saved file will have.
     * @return Name of the stored file.
     * @throws StorageExceptionBadRequest when it cannot save the file.
     */
    override suspend fun storeFileFromUser(file: MultipartFile, fileName: String): String {
        val extension = StringUtils.getFilenameExtension(file.originalFilename)
        val fileSaved = "$fileName.$extension"

        try {
            if (file.isEmpty) {
                throw StorageExceptionBadRequest("Could not store $fileName. File is empty.")
            }
            if (fileSaved.contains("..")) {
                throw StorageExceptionBadRequest("You cannot store $fileName in a parent directory.")
            }
            file.inputStream.use { inputStream ->
                Files.copy(
                    inputStream, rootLocation.resolve(fileSaved),
                    StandardCopyOption.REPLACE_EXISTING
                )
                return fileSaved
            }
        } catch (e: IOException) {
            throw StorageExceptionBadRequest("Could not store $fileName", e)
        }
    }

    /**
     * Function that will load every file from the storage folder.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @return Stream with the paths of every file from the storage folder.
     * @throws StorageExceptionBadRequest when it cannot read a file.
     */
    override fun loadAll(): Stream<Path> {
        return try {
            Files.walk(rootLocation, 1)
                .filter { path -> !path.equals(rootLocation) }
                .map(rootLocation::relativize)
        } catch (e: IOException) {
            throw StorageExceptionBadRequest("Could not read files", e)
        }
    }

    /**
     * Function that will get the path of a given filename.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param fileName Name of the file to be searched.
     * @return Path of the searched file.
     */
    override fun load(fileName: String): Path {
        return rootLocation.resolve(fileName)
    }

    /**
     * Function that will get the resource of a given filename.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param fileName Name of the file to be searched.
     * @return Resource of the searched file.
     * @throws StorageExceptionNotFound when it cannot find or read the file.
     */
    override fun loadAsResource(fileName: String): Resource {
        return try {
            val file = load(fileName)
            val resource = UrlResource(file.toUri())
            if (resource.exists() || resource.isReadable) resource
            else throw StorageExceptionNotFound("Unable to read $fileName")
        } catch (e: MalformedURLException) {
            throw StorageExceptionNotFound("Unable to read $fileName", e)
        }
    }

    /**
     * Function that will delete a file with the given filename from the storage folder.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param fileName The name of the file to be deleted.
     * @throws StorageExceptionBadRequest when it cannot delete the file.
     */
    override fun delete(fileName: String) {
        val name: String = StringUtils.getFilename(fileName).toString()
        try {
            val file = load(name)
            Files.deleteIfExists(file)
        } catch (e: IOException) {
            throw StorageExceptionBadRequest("Unable to delete $fileName", e)
        }
    }

    /**
     * Function that will delete every file in the storage folder.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     */
    override fun deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile())
    }

    /**
     * Function that will get the url from a given filename in the storage folder.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param fileName The name of the file to be searched.
     */
    override fun getUrl(fileName: String): String {
        return MvcUriComponentsBuilder
            .fromMethodName(StorageController::class.java, "serveFile", fileName, null)
            .build().toUriString()
    }
}