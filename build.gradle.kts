import java.time.LocalDate
import java.util.Locale
import java.util.Properties
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class GenerateTextFileTask : DefaultTask() {
    @get:Input
    abstract val content: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun writeFile() {
        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(content.get())
        }
    }
}

plugins {
    alias(libs.plugins.androidApplication)   apply false
    alias(libs.plugins.androidMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatform)  apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler)      apply false
    alias(libs.plugins.kotlinSerialization)  apply false
}

val androidMinSdkVersion     by extra(31)
val androidTargetSdkVersion  by extra(37)
val androidCompileSdkVersion by extra(37)
val androidBuildToolsVersion by extra("37.0.0")

val versionProperties = Properties()
val versionPropertiesFile = rootProject.file("version.properties")
if (versionPropertiesFile.isFile) {
    versionPropertiesFile.inputStream().use(versionProperties::load)
}

fun readVersionPart(projectPropertyName: String, filePropertyName: String): Int {
    val rawValue = findProperty(projectPropertyName)?.toString()?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: versionProperties.getProperty(filePropertyName)?.trim()

    val parsedValue = rawValue?.toIntOrNull()
    if (parsedValue == null || parsedValue !in 0..99) {
        if (rawValue != null) {
            logger.warn("Invalid $filePropertyName='$rawValue'; falling back to 0")
        }
        return 0
    }
    return parsedValue
}

val versionDate = LocalDate.now()
val versionYear = versionDate.year % 100
val versionMonth = versionDate.monthValue
val versionMajor = readVersionPart("verMajor", "ver_Major")
val versionMinor = readVersionPart("verMinor", "ver_Minor")

// YY.MM.ver_Major.ver_Minor is the user-facing version. Android's numeric
// versionCode keeps the same fields in fixed-width positions for monotonicity.
val appVersionName = String.format(Locale.ROOT, "%02d.%02d.%d.%d", versionYear, versionMonth, versionMajor, versionMinor)
val appVersionCode = versionYear * 1_000_000 + versionMonth * 10_000 + versionMajor * 100 + versionMinor
val appVersionRelease = String.format(Locale.ROOT, "%02d.%02d.%d", versionYear, versionMonth, versionMajor)
val appVersionBuild = String.format(Locale.ROOT, "%02d%02d.%d.%d", versionYear, versionMonth, versionMajor, versionMinor)

extra["appVersionName"] = appVersionName
extra["appVersionCode"] = appVersionCode
extra["appVersionRelease"] = appVersionRelease
extra["appVersionBuild"] = appVersionBuild

val generateAppVersionMetadata = tasks.register<GenerateTextFileTask>("generateAppVersionMetadata") {
    notCompatibleWithConfigurationCache("Version metadata includes the current calendar date")
    outputFile.set(rootProject.layout.projectDirectory.file("iosApp/Configuration/Version.xcconfig"))
    content.set(
        """
        // Generated from version.properties. Do not edit manually.
        APP_VERSION_RELEASE = $appVersionRelease
        APP_VERSION_BUILD = $appVersionBuild
        """.trimIndent() + "\n"
    )
}
