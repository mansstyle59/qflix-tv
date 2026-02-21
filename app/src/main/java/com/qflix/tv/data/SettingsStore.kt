
package com.qflix.tv.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "qflix_settings")

class SettingsStore(private val context: Context) {

    private val KEY_START_LIVE = booleanPreferencesKey("start_live")

    val startLiveFlow: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_START_LIVE] ?: false }

    suspend fun setStartLive(value: Boolean) {
        context.dataStore.edit { it[KEY_START_LIVE] = value }
    }
}
