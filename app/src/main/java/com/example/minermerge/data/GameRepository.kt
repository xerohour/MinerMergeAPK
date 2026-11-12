package com.example.minermerge.data

import android.content.Context
import android.content.SharedPreferences
import com.example.minermerge.model.PickaxeItem
import com.example.minermerge.model.GiftItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SavedGameState(
    val coins: Int = 50,
    val currentLevel: Int = 1,
    val pickaxes: List<PickaxeItem> = emptyList(),
    val collectedGifts: List<GiftItem> = emptyList(),
    val usedGiftIds: List<String> = emptyList(),
    val usedIceBlockIds: List<String> = emptyList(),
    val usedTntBlockIds: List<String> = emptyList(),
    val destroyedBlockIds: List<String> = emptyList(),
    val luckyLootLevel: Int = 1,
    val buyQualityLevel: Int = 1,
    val luckyFreqLevel: Int = 1,
    val totalDrops: Int = 0,
    val adminPanelUnlocked: Boolean = false
)

class GameRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("miner_merge_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun loadGameState(): SavedGameState {
        val raw = prefs.getString("saved_state", null) ?: return SavedGameState()
        return try {
            json.decodeFromString<SavedGameState>(raw)
        } catch (e: Exception) {
            SavedGameState()
        }
    }

    fun saveGameState(state: SavedGameState) {
        try {
            val raw = json.encodeToString(state)
            prefs.edit().putString("saved_state", raw).apply()
        } catch (e: Exception) {
            // Ignore error
        }
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}
