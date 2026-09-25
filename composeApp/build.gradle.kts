@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

val androidCompileSdkVersion: Int = rootProject.extra["androidCompileSdkVersion"] as Int
val androidMinSdkVersion:     Int = rootProject.extra["androidMinSdkVersion"]     as Int
val appVersionName: String = rootProject.extra["appVersionName"] as String
val appVersionCode: Int = rootProject.extra["appVersionCode"] as Int

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

val generatedVersionDir = layout.buildDirectory.dir("generated/version/commonMain/kotlin")
val generatedVersionFile = generatedVersionDir.map {
    it.file("org/cf0x/spicecompose/util/GeneratedAppVersion.kt")
}
val generateAppVersionSource = tasks.register<GenerateTextFileTask>("generateAppVersionSource") {
    notCompatibleWithConfigurationCache("Version metadata includes the current calendar date")
    outputFile.set(generatedVersionFile)
    content.set(
        """
        package org.cf0x.spicecompose.util

        // Generated from the root version.properties file. Do not edit manually.
        const val GENERATED_APP_VERSION = "$appVersionName"
        const val GENERATED_APP_VERSION_CODE = $appVersionCode
        """.trimIndent() + "\n"
    )
}

kotlin.sourceSets.getByName("commonMain").kotlin.srcDir(generatedVersionDir)
tasks.configureEach {
    if (name.startsWith("compile") && (name.contains("Kotlin") || name.contains("AndroidMain"))) {
        dependsOn(generateAppVersionSource)
    }
}
tasks.matching { it.name == "embedAndSignAppleFrameworkForXcode" }.configureEach {
    dependsOn(rootProject.tasks.named("generateAppVersionMetadata"))
}

kotlin {
    android {
        namespace = "org.cf0x.spicecompose.compose"
        compileSdk = androidCompileSdkVersion
        minSdk = androidMinSdkVersion
        compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
        androidResources.enable = true
    }

    jvm("desktop") {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
    }
    wasmJs {
        browser {
            binaries.executable()
        }
    }

    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
        listOf(
            iosArm64(),
            iosSimulatorArm64(),
        ).forEach { iosTarget ->
            // cinterop generates bindings; Xcode links the Rust static library.
            iosTarget.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
                binaryOption("bundleId", "org.cf0x.spicecompose.ComposeApp")
            }

            iosTarget.compilations.getByName("main").cinterops.create("spiceBridge") {
                defFile(file("src/iosMain/cinterop/spiceBridge.def"))
                includeDirs(file("src/iosMain/cinterop"))
            }
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val desktopMain = sourceSets.getByName("desktopMain")
        val wasmJsMain = sourceSets.getByName("wasmJsMain")
        val iosMain = sourceSets.findByName("iosMain")

        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.ui.tooling.preview)
            implementation(compose.components.resources)
            implementation(libs.material.icons.extended)
            implementation(libs.materialkolor)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.miuix.ui)
            implementation(libs.miuix.icons)
            implementation(libs.miuix.preference)
            implementation(libs.miuix.blur)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.websockets)
        }

        androidMain.dependencies {
            implementation(libs.jetbrains.compose.ui.tooling)
            implementation(libs.androidx.ui.tooling.preview)
            implementation(libs.androidx.material3)       // dynamicDarkColorScheme
            implementation(libs.androidx.activity.compose)               // BackHandler actual
            implementation(libs.miuix.blur)
            implementation(libs.ktor.client.okhttp)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.jna)
        }

        wasmJsMain.dependencies {
        }

        iosMain?.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.cf0x.spicecompose.MainKt"

        val currentJavaHome = System.getProperty("java.home") ?: ""
        val hasJpackage = File(currentJavaHome, "bin/jpackage.exe").exists()
            || File(currentJavaHome, "bin/jpackage").exists()
        if (!hasJpackage) {
            val candidatePaths = listOfNotNull(
                "D:/DevHub/Java/jdk-25.0.4.1",
                System.getenv("JAVA_HOME"),
                "${System.getProperty("user.home")}/.jdks/jbr-21.0.11",
                "C:/Program Files/Java/latest"
            )
            val validJdk = candidatePaths.firstOrNull { path ->
                File(path, "bin/jpackage.exe").exists() || File(path, "bin/jpackage").exists()
            }
            if (validJdk != null) {
                javaHome = validJdk
            }
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "SpiceCompose"
            packageVersion = rootProject.extra["appVersionRelease"] as String
            description = "SpiceCompose Desktop Application"
            vendor = "SpiceCompose"

            windows {
                shortcut = true
                menu = true
                menuGroup = "SpiceCompose"
                dirChooser = true
                perUserInstall = false
                upgradeUuid = "c98342f1-638a-4467-b50a-81a6c0f2142e"
            }
        }
    }
}

// Build and package the Rust C ABI library used by the desktop target. This is
// the same native core linked by Android and iOS; desktop loads it through JNA.
val desktopRustLibraryName = System.mapLibraryName("spice_backend")
val desktopRustResourceDir = layout.buildDirectory.dir("generated/resources/desktopMain/native")
val buildDesktopRust = tasks.register<Exec>("buildDesktopRust") {
    workingDir(rootProject.layout.projectDirectory.dir("rust-backend"))
    commandLine("cargo", "build", "--release", "--lib")
}
val stageDesktopRust = tasks.register<Copy>("stageDesktopRust") {
    dependsOn(buildDesktopRust)
    from(rootProject.layout.projectDirectory.dir("rust-backend/target/release")) {
        include(desktopRustLibraryName)
    }
    into(desktopRustResourceDir)
}
kotlin.sourceSets.getByName("desktopMain").resources.srcDir(desktopRustResourceDir)
tasks.named("desktopProcessResources") {
    dependsOn(stageDesktopRust)
}

// Copy Wasm distribution to Rust backend's static directory
tasks.register<Copy>("deployWasmToRustBackend") {
    dependsOn(tasks.matching { it.name == "wasmJsBrowserDistribution" })
    from(layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))
    into(rootProject.layout.projectDirectory.dir("rust-backend/static"))
}

compose.resources {
    publicResClass = true
    generateResClass = always
}
