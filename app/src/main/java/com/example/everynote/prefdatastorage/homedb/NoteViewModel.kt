// NoteViewModel.kt - Fixed version
package com.example.everynote.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.everynote.prefdatastorage.homedb.NoteEntity
import com.example.everynote.prefdatastorage.homedb.SharedPreferencesHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NoteViewModel(private val helper: SharedPreferencesHelper) : ViewModel() {
    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(listOf("General"))
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _notes.value = helper.getNotes()
        val storedCategories = helper.getCategories()
        if (storedCategories.isNotEmpty()) {
            _categories.value = storedCategories
        }
    }

    fun insertNote(note: NoteEntity) {
        val currentNotes = _notes.value
        val nextId = (currentNotes.maxOfOrNull { it.id } ?: 0) + 1
        val newNote = note.copy(id = nextId)
        val updatedNotes = currentNotes + newNote
        _notes.value = updatedNotes
        helper.saveNotes(updatedNotes)
    }

    fun deleteNote(note: NoteEntity) {
        val updatedNotes = _notes.value.filter { it.id != note.id }
        _notes.value = updatedNotes
        helper.saveNotes(updatedNotes)
    }

    fun updateNote(updatedNote: NoteEntity) {
        val currentNotes = _notes.value.toMutableList()
        val index = currentNotes.indexOfFirst { it.id == updatedNote.id }
        if (index != -1) {
            currentNotes[index] = updatedNote
            _notes.value = currentNotes
            helper.saveNotes(currentNotes)
        }
    }

    fun addCategory(category: String) {
        val currentCategories = _categories.value
        if (category !in currentCategories) {
            val updatedCategories = currentCategories + category
            _categories.value = updatedCategories
            helper.saveCategories(updatedCategories)
        }
    }
}

class NoteViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val sharedPrefHelper = SharedPreferencesHelper(context.applicationContext)
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(sharedPrefHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}