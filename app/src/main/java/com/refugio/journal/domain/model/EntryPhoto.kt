package com.refugio.journal.domain.model

data class EntryPhoto(
    val id: Long = 0L,
    val entryId: Long = 0L,
    val fileName: String,
    val caption: String? = null,
    val orderIndex: Int = 0
)
