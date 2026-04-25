package com.uniflow.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {
    private val DEPT_KEY = stringPreferencesKey("department")
    private val POS_KEY = stringPreferencesKey("position")
    private val NAME_KEY = stringPreferencesKey("name_surname")
    private val ROLE_KEY = stringPreferencesKey("role")

    val userDepartment: Flow<String?> = context.dataStore.data.map { it[DEPT_KEY] }
    val userPosition: Flow<String?> = context.dataStore.data.map { it[POS_KEY] }
    val userName: Flow<String?> = context.dataStore.data.map { it[NAME_KEY] }
    val userRole: Flow<String?> = context.dataStore.data.map { it[ROLE_KEY] }

    suspend fun saveUserData(name: String, dept: String, pos: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[NAME_KEY] = name
            preferences[DEPT_KEY] = dept
            preferences[POS_KEY] = pos
            preferences[ROLE_KEY] = role
        }
    }
}
