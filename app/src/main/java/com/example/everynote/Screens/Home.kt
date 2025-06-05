package com.example.everynote.Screens

import android.app.DatePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.everynote.prefdatastorage.UserPreferences
import com.example.everynote.prefdatastorage.homedb.NoteEntity
import com.example.everynote.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: NoteViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("General") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var inputText by remember { mutableStateOf("") }
    var showProfileMenu by remember { mutableStateOf(false) }

    val user by userPrefs.userFlow.collectAsState(initial = null)
    val currentUser = user

    val todayDate = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    val notes by viewModel.notes.collectAsState(initial = emptyList())
    val categories by viewModel.categories.collectAsState(initial = emptyList())

    val allCategories = remember(categories) {
        listOf("General") + categories.filter { it != "General" && it != "+ Create" } + listOf("+ Create")
    }

    val filteredNotes = remember(notes, searchText, selectedCategory) {
        notes.filter { note ->
            note.category == selectedCategory &&
                    (searchText.isBlank() || note.content.contains(searchText, ignoreCase = true))
        }
    }

    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val formattedDate =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
                inputText = if (inputText.isBlank()) formattedDate else "$inputText\n$formattedDate"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            TopBar(
                currentUser = currentUser,
                showProfileMenu = showProfileMenu,
                onProfileMenuToggle = { showProfileMenu = !showProfileMenu },
                onLogout = {
                    scope.launch {
                        userPrefs.saveUser("", "", "", "")
                        onLogout()
                    }
                },
                todayDate = todayDate
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchAndDatePicker(
                searchText = searchText,
                onSearchChange = { searchText = it },
                onDatePickerClick = { datePickerDialog.show() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CategoriesRow(
                categories = allCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    if (category == "+ Create") {
                        showCreateDialog = true
                    } else {
                        selectedCategory = category
                        inputText = ""
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            NoteInput(
                category = selectedCategory,
                inputText = inputText,
                onInputChange = { inputText = it },
                onSaveClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.insertNote(
                            NoteEntity(
                                content = inputText.trim(),
                                category = selectedCategory,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        inputText = ""
                        scope.launch {
                            snackbarHostState.showSnackbar("Note saved!")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredNotes.isEmpty()) {
                EmptyNotesMessage(searchText = searchText, category = selectedCategory)
            } else {
                NotesList(notes = filteredNotes, onDelete = { note ->
                    viewModel.deleteNote(note)
                    scope.launch {
                        snackbarHostState.showSnackbar("Note deleted")
                    }
                })
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                newCategoryName = ""
            },
            title = { Text("Create New Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    placeholder = { Text("Enter category name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    maxLines = 1,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newCategoryName.trim()
                        if (trimmed.isNotEmpty() && trimmed != "+ Create") {
                            viewModel.addCategory(trimmed)
                            selectedCategory = trimmed
                        }
                        showCreateDialog = false
                        newCategoryName = ""
                        inputText = ""
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateDialog = false
                        newCategoryName = ""
                    }
                ) { Text("Cancel") }
            }
        )
    }
}


@Composable
private fun TopBar(
    currentUser: com.example.everynote.prefdatastorage.User?,
    showProfileMenu: Boolean,
    onProfileMenuToggle: () -> Unit,
    onLogout: () -> Unit,
    todayDate: String
) {
    Box {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ProfilePicture(
                user = currentUser,
                onClick = onProfileMenuToggle
            )

            Text(
                text = todayDate,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        DropdownMenu(
            expanded = showProfileMenu,
            onDismissRequest = onProfileMenuToggle,
            modifier = Modifier.width(IntrinsicSize.Min)
        ) {
            DropdownMenuItem(text = { Text("Logout") }, onClick = onLogout)
        }
    }
}

@Composable
private fun ProfilePicture(
    user: com.example.everynote.prefdatastorage.User?,
    onClick: () -> Unit
) {
    if (user != null && user.photoUrl.isNotBlank()) {
        AsyncImage(
            model = user.photoUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { onClick() }
        )
    } else {
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onClick() },
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SearchAndDatePicker(
    searchText: String,
    onSearchChange: (String) -> Unit,
    onDatePickerClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchChange,
            placeholder = { Text("Search your notes") },
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(50.dp),
            singleLine = true,

        )
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(onClick = onDatePickerClick) {
            Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
        }
    }
}

@Composable
private fun CategoriesRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { category ->
            CategoryItem(
                text = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun CategoryItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun NoteInput(
    category: String,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = { Text("Write in $category...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = inputText.isNotBlank()
        ) {
            Text("Save Note")
        }
    }
}

@Composable
private fun EmptyNotesMessage(
    searchText: String,
    category: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                text = if (searchText.isBlank())
                    "No notes in $category yet"
                else
                    "No notes found for \"$searchText\"",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotesList(
    notes: List<NoteEntity>,
    onDelete: (NoteEntity) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(notes, key = { it.id }) { note ->
            NoteItem(note = note, onDelete = { onDelete(note) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: NoteEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* TODO: Open/edit note if needed */ },
                onLongClick = onDelete
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = note.content,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete note",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


