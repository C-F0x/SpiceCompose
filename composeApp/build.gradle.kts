@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

val androidCompileSdkVersion: Int = rootProject.extra["androidCompileSdkVersion"] as Int
val androidMinSdkVersion:     Int = rootProject.extra["androidMinSdkVersion"]     as Int

kotlin {
    android {
        namespace = "org.cf0x.spicecompose.compose"
        compileSdk = androidCompileSdkVersion
        minSdk = androidMinSdkVersion
        compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
        androidResources.enable = true
    }

    jvm("desktop")
    wasmJs {
        browser()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        // cinterop generates bindings; Xcode links the Rust static library.
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }

        iosTarget.compilations.getByName("main").cinterops.create("spiceBridge") {
            defFile(file("src/iosMain/cinterop/spiceBridge.def"))
            includeDirs(file("src/iosMain/cinterop"))
        }
    }

    applyDefaultHierarchyTemplate()

    // Keep iosX64 disabled: current Compose/Miuix artifacts lack Intel
    // simulator variants.

    sourceSets {
        val desktopMain = sourceSets.getByName("desktopMain")
        val wasmJsMain = sourceSets.getByName("wasmJsMain")
        val iosMain = sourceSets.getByName("iosMain")

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
            implementation(libs.ktor.client.java)
        }

        wasmJsMain.dependencies {
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.cf0x.spicecompose.MainKt"
    }
}

// Copy Wasm distribution to Rust backend's static directory
tasks.register<Copy>("deployWasmToRustBackend") {
    dependsOn("composeCompatibilityBrowserDistribution")
    from(layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))
    into(rootProject.layout.projectDirectory.dir("rust-backend/static"))
}

compose.resources {
    publicResClass = true
    generateResClass = always
}
