package com.example.everynote.prefdatastorage

// UserPreferences.kt

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "user_prefs")

    private val NAME_KEY = stringPreferencesKey("name")
    private val PHOTO_URL_KEY = stringPreferencesKey("photo_url")
    private val EMAIL_KEY = stringPreferencesKey("email")
    private val PASSWORD_KEY = stringPreferencesKey("password")

    suspend fun saveUser(name: String, photoUrl: String, email: String, password: String) {
        context.dataStore.edit {
            it[NAME_KEY] = name
            it[PHOTO_URL_KEY] = photoUrl
            it[EMAIL_KEY] = email
            it[PASSWORD_KEY] = password
        }
    }

    val userFlow: Flow<User> = context.dataStore.data.map { prefs ->
        User(
            name = prefs[NAME_KEY] ?: "",
            photoUrl = prefs[PHOTO_URL_KEY] ?: "",
            email = prefs[EMAIL_KEY] ?: "",
            password = prefs[PASSWORD_KEY] ?: ""
        )
    }
}

data class User(
    val name: String,
    val photoUrl: String,
    val email: String,
    val password: String
)
