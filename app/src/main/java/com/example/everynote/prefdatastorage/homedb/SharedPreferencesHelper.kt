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

    fun saveNotes(notes: List<NoteEntity>) {
        sharedPreferences.edit {
            putString("notes", gson.toJson(notes))
        }
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

    fun saveCategories(categories: List<String>) {
        sharedPreferences.edit {
            putString("categories", gson.toJson(categories))
        }
    }

    fun getCategories(): List<String> {
        val json = sharedPreferences.getString("categories", null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
}
