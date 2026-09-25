import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

val androidMinSdkVersion:     Int    = rootProject.extra["androidMinSdkVersion"] as Int
val androidTargetSdkVersion:  Int    = rootProject.extra["androidTargetSdkVersion"] as Int
val androidCompileSdkVersion: Int    = rootProject.extra["androidCompileSdkVersion"] as Int
val androidBuildToolsVersion: String = rootProject.extra["androidBuildToolsVersion"] as String
val appVersionName: String = rootProject.extra["appVersionName"] as String
val appVersionCode: Int = rootProject.extra["appVersionCode"] as Int

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
