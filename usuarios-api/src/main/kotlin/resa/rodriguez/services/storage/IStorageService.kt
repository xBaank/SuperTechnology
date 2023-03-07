package resa.rodriguez.services.storage

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import resa.rodriguez.exceptions.AddressExceptionNotFound
import java.nio.file.Path
import java.util.stream.Stream

/**
 * Interface for the Storage Service.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
interface IStorageService {
    fun initStorageDirectory()
    fun store(file: MultipartFile): String
    fun loadAll(): Stream<Path>
    fun load(fileName: String): Path
    fun loadAsResource(fileName: String): Resource
    fun delete(fileName: String)
    fun deleteAll()
    fun getUrl(fileName: String): String
    suspend fun storeFileFromUser(file: MultipartFile, fileName: String): String
}
