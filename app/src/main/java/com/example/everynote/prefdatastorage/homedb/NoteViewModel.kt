import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.everynote.prefdatastorage.homedb.NoteEntity
import com.example.everynote.prefdatastorage.homedb.SharedPreferencesHelper
import kotlinx.coroutines.launch

class NoteViewModel(private val helper: SharedPreferencesHelper) : ViewModel() {
    private val _notes = mutableStateListOf<NoteEntity>()
    val notes: List<NoteEntity> get() = _notes

    var categories = mutableStateListOf("General")
        private set

    init {
        _notes.addAll(helper.getNotes())
        val storedCategories = helper.getCategories()
        if (storedCategories.isNotEmpty()) {
            categories.clear()
            categories.addAll(storedCategories)
        }
    }

    fun insertNote(note: NoteEntity) {
        val nextId = (_notes.maxOfOrNull { it.id } ?: 0) + 1
        val newNote = note.copy(id = nextId)
        _notes.add(newNote)
        helper.saveNotes(_notes)
    }

    fun deleteNote(note: NoteEntity) {
        _notes.removeIf { it.id == note.id }
        helper.saveNotes(_notes)
    }

    fun updateNote(updatedNote: NoteEntity) {
        val index = _notes.indexOfFirst { it.id == updatedNote.id }
        if (index != -1) {
            _notes[index] = updatedNote
            helper.saveNotes(_notes)
        }
    }

    fun addCategory(category: String) {
        if (category !in categories) {
            categories.add(category)
            helper.saveCategories(categories)
        }
    }
}

class NoteViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val sharedPrefHelper = SharedPreferencesHelper(context.applicationContext)
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            return NoteViewModel(sharedPrefHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
