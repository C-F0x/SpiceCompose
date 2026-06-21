package org.cf0x.spicecompose.ui.screen.controllers.diy

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DiyRepository(private val settings: Settings = Settings()) {
    private val key = "diy_layouts"
    private val selectedKey = "diy_selected"
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun getAll(): List<DiyLayout> {
        val raw = settings.getStringOrNull(key) ?: return emptyList()
        return try { json.decodeFromString<List<DiyLayout>>(raw) } catch (_: Exception) { emptyList() }
    }

    fun getForGame(gameModel: String): List<DiyLayout> =
        getAll().filter { it.gameModel == gameModel }

    fun getSelected(): String? = settings.getStringOrNull(selectedKey)

    fun setSelected(id: String) { settings[selectedKey] = id }

    fun getById(id: String): DiyLayout? = getAll().find { it.name == id }

    fun save(layout: DiyLayout) {
        val all = getAll().toMutableList()
        val idx = all.indexOfFirst { it.name == layout.name }
        if (idx >= 0) all[idx] = layout else all.add(layout)
        settings[key] = json.encodeToString(all)
    }

    fun delete(name: String) {
        val all = getAll().filter { it.name != name }
        settings[key] = json.encodeToString(all)
        if (getSelected() == name) settings.remove(selectedKey)
    }
}
