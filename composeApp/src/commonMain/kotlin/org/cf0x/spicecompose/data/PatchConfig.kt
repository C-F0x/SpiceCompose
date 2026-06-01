package org.cf0x.spicecompose.data

import kotlinx.serialization.Serializable
import org.cf0x.spicecompose.network.spiceapi.SpiceConnection
import org.cf0x.spicecompose.network.spiceapi.wrappers.memoryRead
import org.cf0x.spicecompose.network.spiceapi.wrappers.memoryWrite
import org.cf0x.spicecompose.network.spiceapi.wrappers.memorySignature
import kotlin.math.max

enum class PatchStatus {
    Unknown,
    Enabled,
    Disabled
}

@Serializable
sealed class PatchConfig {
    abstract val name: String
    abstract val description: String
    abstract val gameCode: String
    abstract val dateCodeMin: Int
    abstract val dateCodeMax: Int
    abstract var preset: Boolean
    abstract var online: Boolean

    fun isInRange(dateCode: Int): Boolean {
        if (dateCodeMin == 0 && dateCodeMax == 0) return true
        if (dateCodeMax == 0) return dateCode >= dateCodeMin
        return dateCode in dateCodeMin..dateCodeMax
    }

    abstract suspend fun getStatus(connection: SpiceConnection): PatchStatus
    abstract suspend fun setStatus(connection: SpiceConnection, status: PatchStatus): Boolean
    
    protected open suspend fun detectDllName(connection: SpiceConnection, dllName: String, gameModel: String, gameRev: String): String {
        // Omnimix detection logic
        if (gameModel == "LDJ" && gameRev == "X") {
            return try {
                connection.memoryRead("bm2dx_omni.dll", 302252, 1)
                "bm2dx_omni.dll"
            } catch (e: Exception) {
                "bm2dx.dll"
            }
        }
        return dllName
    }
}

@Serializable
data class MemoryPatchData(
    val dllName: String = "",
    val dataEnabled: String = "",
    val dataDisabled: String = "",
    val dataOffset: Int = 0
)

@Serializable
data class MemoryPatchConfig(
    override val name: String,
    override val description: String = "",
    override val gameCode: String = "",
    override val dateCodeMin: Int = 0,
    override val dateCodeMax: Int = 0,
    override var preset: Boolean = false,
    override var online: Boolean = false,
    val patches: List<MemoryPatchData> = emptyList()
) : PatchConfig() {
    override suspend fun getStatus(connection: SpiceConnection): PatchStatus {
        val states = patches.map { patch ->
            val dll = detectDllName(connection, patch.dllName, "", "") // TODO: Pass real gameModel/Rev
            try {
                val currentData = connection.memoryRead(dll, patch.dataOffset, max(patch.dataEnabled.length, patch.dataDisabled.length) / 2)
                when {
                    patch.dataEnabled.isNotEmpty() && currentData.startsWith(patch.dataEnabled, ignoreCase = true) -> PatchStatus.Enabled
                    patch.dataDisabled.isNotEmpty() && currentData.startsWith(patch.dataDisabled, ignoreCase = true) -> PatchStatus.Disabled
                    else -> PatchStatus.Unknown
                }
            } catch (e: Exception) {
                PatchStatus.Unknown
            }
        }

        if (states.any { it == PatchStatus.Unknown }) return PatchStatus.Unknown
        val hasEnabled = states.any { it == PatchStatus.Enabled }
        val hasDisabled = states.any { it == PatchStatus.Disabled }
        
        return if (hasEnabled && !hasDisabled) PatchStatus.Enabled
        else if (!hasEnabled && hasDisabled) PatchStatus.Disabled
        else PatchStatus.Unknown
    }

    override suspend fun setStatus(connection: SpiceConnection, status: PatchStatus): Boolean {
        if (status == PatchStatus.Unknown) return false
        return try {
            patches.forEach { patch ->
                val dll = detectDllName(connection, patch.dllName, "", "")
                val data = if (status == PatchStatus.Enabled) patch.dataEnabled else patch.dataDisabled
                connection.memoryWrite(dll, data, patch.dataOffset)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

@Serializable
data class SignaturePatchConfig(
    override val name: String,
    override val description: String = "",
    override val gameCode: String = "",
    override val dateCodeMin: Int = 0,
    override val dateCodeMax: Int = 0,
    override var preset: Boolean = false,
    override var online: Boolean = false,
    val dllName: String,
    val signature: String,
    val replacement: String,
    val offset: Int = 0,
    val usage: Int = 0
) : PatchConfig() {
    // Logic for signature patches involves tracking rawOffset and original data
    // For simplicity in first pass, we might need a cache or more complex state management
    // Flutter version stores rawOffset in memory. 
    private var rawOffset: Int = 0
    private var dataDisabled: String = ""

    override suspend fun getStatus(connection: SpiceConnection): PatchStatus {
        return if (rawOffset > 0) PatchStatus.Enabled else PatchStatus.Disabled
    }

    override suspend fun setStatus(connection: SpiceConnection, status: PatchStatus): Boolean {
        val dll = detectDllName(connection, dllName, "", "")
        return try {
            if (status == PatchStatus.Enabled) {
                val foundOffset = connection.memorySignature(dll, signature, "", offset, usage)
                if (foundOffset > 0) {
                    rawOffset = foundOffset
                    dataDisabled = connection.memoryRead(dll, rawOffset, replacement.length / 2)
                    val secondOffset = connection.memorySignature(dll, signature, replacement, offset, usage)
                    foundOffset == secondOffset
                } else false
            } else {
                if (rawOffset > 0) {
                    connection.memoryWrite(dll, dataDisabled, rawOffset)
                    rawOffset = 0
                    dataDisabled = ""
                    true
                } else false
            }
        } catch (e: Exception) {
            false
        }
    }
}
