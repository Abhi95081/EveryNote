package com.example.everynote.prefdatastorage.homedb

data class NoteEntity(
    val id: Int = 0,
    val content: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)
