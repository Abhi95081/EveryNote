package com.example.everynote.prefdatastorage.homedb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel(
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : ViewModel() {

    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes.asStateFlow()

    init {
        _notes.value = sharedPreferencesHelper.getNotes()
    }

    fun insertNote(note: NoteEntity) {
        viewModelScope.launch {
            sharedPreferencesHelper.saveNote(note)
            _notes.value = sharedPreferencesHelper.getNotes()
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            sharedPreferencesHelper.deleteNote(note)
            _notes.value = sharedPreferencesHelper.getNotes()
        }
    }
}
