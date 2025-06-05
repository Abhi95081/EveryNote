package com.example.everynote.prefdatastorage.homedb

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveNote(note: NoteEntity) {
        val notes = getNotes().toMutableList()
        notes.add(note)
        sharedPreferences.edit { putString("notes", gson.toJson(notes)) }
    }

    fun getNotes(): List<NoteEntity> {
        val json = sharedPreferences.getString("notes", null)
        return if (json != null) {
            val type = object : TypeToken<List<NoteEntity>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun deleteNote(note: NoteEntity) {
        val notes = getNotes().toMutableList()
        notes.remove(note)
        sharedPreferences.edit { putString("notes", gson.toJson(notes)) }
    }
}
