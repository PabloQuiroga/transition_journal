package com.refugio.journal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.refugio.journal.domain.model.EntryPhoto

@Entity(
    tableName = "entry_photos",
    foreignKeys = [
        ForeignKey(
            entity = JournalEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["entryId"])]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val entryId: Long,
    val fileName: String,
    val absolutePath: String = "",
    val caption: String? = null,
    val orderIndex: Int = 0
) {
    fun toDomainModel(): EntryPhoto {
        return EntryPhoto(
            id = id,
            entryId = entryId,
            fileName = fileName,
            caption = caption,
            orderIndex = orderIndex
        )
    }

    companion object {
        fun fromDomainModel(photo: EntryPhoto, entryId: Long, absolutePath: String = ""): PhotoEntity {
            return PhotoEntity(
                id = photo.id,
                entryId = entryId,
                fileName = photo.fileName,
                absolutePath = absolutePath,
                caption = photo.caption,
                orderIndex = photo.orderIndex
            )
        }
    }
}
