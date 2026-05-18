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
