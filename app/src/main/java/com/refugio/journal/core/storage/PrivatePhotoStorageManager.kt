package com.refugio.journal.core.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.refugio.journal.domain.repository.PhotoStorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrivatePhotoStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PhotoStorageManager {

    companion object {
        private const val PRIVATE_PHOTOS_DIR = "private_photos"
        private const val SHARED_PHOTOS_DIR = "shared_photos"
        private const val FILE_PROVIDER_AUTHORITY = "com.refugio.journal.fileprovider"
    }

    private val photosDir: File
        get() {
            val dir = File(context.filesDir, PRIVATE_PHOTOS_DIR)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    private val shareCacheDir: File
        get() {
            val dir = File(context.cacheDir, SHARED_PHOTOS_DIR)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    override suspend fun importPhotoToPrivateStorage(sourceUriString: String): Result<String> {
        return runCatching {
            val sourceUri = Uri.parse(sourceUriString)
            val fileName = "${UUID.randomUUID()}.jpg"
            val targetFile = File(photosDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Could not open input stream from URI: $sourceUriString")

            fileName
        }
    }

    override suspend fun deletePrivatePhoto(fileName: String): Result<Unit> {
        return runCatching {
            val targetFile = File(photosDir, fileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }
            Unit
        }
    }

    override suspend fun prepareShareableFile(fileName: String): Result<String> {
        return runCatching {
            val privateFile = File(photosDir, fileName)
            if (!privateFile.exists()) {
                throw IllegalStateException("Private photo does not exist: $fileName")
            }

            val cachedFile = File(shareCacheDir, fileName)
            privateFile.copyTo(cachedFile, overwrite = true)

            val shareUri = FileProvider.getUriForFile(
                context,
                FILE_PROVIDER_AUTHORITY,
                cachedFile
            )

            shareUri.toString()
        }
    }

    override fun clearShareCache() {
        if (shareCacheDir.exists()) {
            shareCacheDir.listFiles()?.forEach { file ->
                file.delete()
            }
        }
    }
}
