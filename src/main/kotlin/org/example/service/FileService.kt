package org.example.service
import io.ktor.features.BadRequestException
import io.ktor.features.UnsupportedMediaTypeException
import io.ktor.http.ContentType
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.dto.responce.AttachmentResponseDto
import org.example.model.AttachmentType
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class FileService(private val uploadPath: String) {
    private val images = listOf(ContentType.Image.JPEG, ContentType.Image.PNG)

    init {
        val path = Paths.get(uploadPath)
        if (Files.notExists(path)) {
            Files.createDirectory(path)
        }
    }

    suspend fun save(multipart: MultiPartData): AttachmentResponseDto {
        var response: AttachmentResponseDto? = null
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    if (part.name == "file") {
                        // use Apache Tika for content detection
                        if (!images.contains(part.contentType)) {
                            throw UnsupportedMediaTypeException(part.contentType ?: ContentType.Any)
                        }
                        val ext = File(part.originalFileName).extension
                        val name = "${UUID.randomUUID()}.$ext"
                        val path = Paths.get(uploadPath, name)
                        part.streamProvider().use {
                            withContext(Dispatchers.IO) {
                                Files.copy(it, path)
                            }
                        }
                        part.dispose()
                        response = AttachmentResponseDto(name, AttachmentType.IMAGE)
                        return@forEachPart
                    }
                }
            }
            part.dispose()
        }
        return response ?: throw BadRequestException("No file field in request")
    }
}