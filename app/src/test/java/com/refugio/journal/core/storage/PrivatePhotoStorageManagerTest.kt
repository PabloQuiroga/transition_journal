package com.refugio.journal.core.storage

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class PrivatePhotoStorageManagerTest {

    private lateinit var context: Context
    private lateinit var storageManager: PrivatePhotoStorageManager

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        storageManager = PrivatePhotoStorageManager(context)
    }

    @Test
    fun `deletePrivatePhoto removes physical file from private_photos directory`() = runTest {
        val photosDir = File(context.filesDir, "private_photos")
        photosDir.mkdirs()
        val testFile = File(photosDir, "test_image.jpg")
        testFile.writeText("fake image bytes")

        assertTrue(testFile.exists())

        val result = storageManager.deletePrivatePhoto("test_image.jpg")

        assertTrue(result.isSuccess)
        assertFalse(testFile.exists())
    }

    @Test
    fun `clearShareCache removes files from share cache directory`() = runTest {
        val cacheDir = File(context.cacheDir, "shared_photos")
        cacheDir.mkdirs()
        val cacheFile = File(cacheDir, "cached_image.jpg")
        cacheFile.writeText("fake image bytes")

        assertTrue(cacheFile.exists())

        storageManager.clearShareCache()

        assertFalse(cacheFile.exists())
    }
}
