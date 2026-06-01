package org.cf0x.spicecompose.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class PatchRepository(private val settings: Settings = Settings()) {
    private val key = "patches_list"
    
    private val patchJson = Json {
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            polymorphic(PatchConfig::class) {
                subclass(MemoryPatchConfig::class)
                subclass(SignaturePatchConfig::class)
            }
        }
    }

    fun getCustomPatches(): List<PatchConfig> {
        val json = settings.getStringOrNull(key) ?: return emptyList()
        return try {
            patchJson.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCustomPatches(patches: List<PatchConfig>) {
        val json = patchJson.encodeToString(patches)
        settings[key] = json
    }

    fun addCustomPatch(patch: PatchConfig) {
        val patches = getCustomPatches().toMutableList()
        patches.add(patch)
        saveCustomPatches(patches)
    }

    fun deleteCustomPatch(name: String) {
        val patches = getCustomPatches().filter { it.name != name }
        saveCustomPatches(patches)
    }
}
