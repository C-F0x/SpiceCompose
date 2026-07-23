import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val androidMinSdkVersion:     Int    by rootProject.extra
val androidTargetSdkVersion:  Int    by rootProject.extra
val androidCompileSdkVersion: Int    by rootProject.extra
val androidBuildToolsVersion: String by rootProject.extra

// Read APP_VERSION / APP_VERSION_CODE from shared source of truth.
val versionSrc = file("../composeApp/src/commonMain/kotlin/org/cf0x/spicecompose/util/AppVersion.kt").readText()
val appVersionName = Regex("""APP_VERSION\s*=\s*"(.+)"""").find(versionSrc)!!.groupValues[1]
val appVersionCode = Regex("""APP_VERSION_CODE\s*=\s*(\d+)""").find(versionSrc)!!.groupValues[1].toInt()

android {
    namespace         = "org.cf0x.spicecompose"
    compileSdk        = androidCompileSdkVersion
    buildToolsVersion = androidBuildToolsVersion

    defaultConfig {
        applicationId = "org.cf0x.spicecompose"
        minSdk        = androidMinSdkVersion
        targetSdk     = androidTargetSdkVersion
        versionCode   = appVersionCode
        versionName   = appVersionName
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    dependenciesInfo {
        includeInApk    = false
        includeInBundle = false
    }
}

kotlin {
    target {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    dependencies {
        implementation(projects.composeApp)
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.lifecycle.runtime.compose)
        implementation(libs.foundation)
    }
}
