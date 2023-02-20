package resa.rodriguez.services.storage

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path
import java.util.stream.Stream

interface IStorageService {
    fun init()
    fun store(file: MultipartFile): String
    fun loadAll(): Stream<Path>
    fun load(fileName: String): Path
    fun loadAsResource(fileName: String): Resource
    fun delete(fileName: String)
    fun deleteAll()
    fun getUrl(fileName: String): String
    fun store(file: MultipartFile, fileName: String): String
}
