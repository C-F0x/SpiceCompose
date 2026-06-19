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
    }

    jvm("desktop")

    sourceSets {
        val desktopMain = sourceSets.getByName("desktopMain")

        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.ui.tooling.preview)
            implementation(libs.components.resources)
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
        }

        androidMain.dependencies {
            implementation(libs.jetbrains.compose.ui.tooling)
            implementation(libs.androidx.ui.tooling.preview)
            implementation("androidx.compose.material3:material3")       // dynamicDarkColorScheme
            implementation(libs.androidx.activity.compose)               // BackHandler actual
            implementation("top.yukonga.miuix.kmp:miuix-blur-android:0.9.2")
            implementation(libs.ktor.client.okhttp)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.java)
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.cf0x.spicecompose.MainKt"
    }
}
