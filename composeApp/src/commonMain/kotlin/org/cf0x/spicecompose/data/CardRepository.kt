package org.cf0x.spicecompose.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CardRepository(private val settings: Settings = Settings()) {
    private val key = "cards_list"

    fun getCards(): List<CardConfig> {
        val json = settings.getStringOrNull(key) ?: return emptyList()
        return try {
            Json.decodeFromString<List<CardConfig>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCards(cards: List<CardConfig>) {
        val json = Json.encodeToString(cards)
        settings[key] = json
    }

    fun addCard(card: CardConfig) {
        val cards = getCards().toMutableList()
        cards.add(card)
        saveCards(cards)
    }

    fun updateCard(card: CardConfig) {
        val cards = getCards().map { if (it.id == card.id) card else it }
        saveCards(cards)
    }

    fun deleteCard(id: String) {
        val cards = getCards().filter { it.id != id }
        saveCards(cards)
    }
    
    fun setActive(id: String?) {
        val cards = getCards().map { it.copy(active = it.id == id) }
        saveCards(cards)
    }
}
