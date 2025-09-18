package com.example.everynote.prefdatastorage


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    private val nameKey = stringPreferencesKey("name")
    private val photoUrlKey = stringPreferencesKey("photo_url")
    private val emailKey = stringPreferencesKey("email")
    private val passwordKey = stringPreferencesKey("password")

    val userFlow: Flow<User> = context.dataStore.data.map { preferences ->
        User(
            name = preferences[nameKey] ?: "",
            photoUrl = preferences[photoUrlKey] ?: "",
            email = preferences[emailKey] ?: "",
            password = preferences[passwordKey] ?: ""
        )
    }

    suspend fun saveUser(name: String, photoUrl: String, email: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[nameKey] = name
            preferences[photoUrlKey] = photoUrl
            preferences[emailKey] = email
            preferences[passwordKey] = password
        }
    }
}

data class User(
    val name: String,
    val photoUrl: String,
    val email: String,
    val password: String
)
