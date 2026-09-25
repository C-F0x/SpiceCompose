@file:Suppress("UnstableApiUsage")

fun removeEnvVar(name: String) {
    try {
        val peClass = Class.forName("java.lang.ProcessEnvironment")
        listOf("theEnvironment", "theCaseInsensitiveEnvironment").forEach { fieldName ->
            try {
                val f = peClass.getDeclaredField(fieldName)
                f.isAccessible = true
                (f.get(null) as? MutableMap<*, *>)?.remove(name)
            } catch (_: Throwable) {}
        }
        try {
            val f = peClass.getDeclaredField("theUnmodifiableEnvironment")
            f.isAccessible = true
            val unmodifiable = f.get(null)
            val mField = unmodifiable?.javaClass?.getDeclaredField("m")
            mField?.isAccessible = true
            (mField?.get(unmodifiable) as? MutableMap<*, *>)?.remove(name)
        } catch (_: Throwable) {}
    } catch (_: Throwable) {}

    try {
        val jnaFunction = Class.forName("com.sun.jna.Function")
        val getFunction = jnaFunction.getMethod("getFunction", String::class.java, String::class.java)
        val setEnvFunc = getFunction.invoke(null, "kernel32", "SetEnvironmentVariableW")
        val invokeInt = jnaFunction.getMethod("invokeInt", Array<Any>::class.java)
        setEnvFunc?.let {
            invokeInt.invoke(it, arrayOf(arrayOf<Any?>(name, null)))
        }
    } catch (_: Throwable) {}
}

removeEnvVar("ANDROID_PREFS_ROOT")
removeEnvVar("ANDROID_USERS_ROOT")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-js/maven")
    }
}

rootProject.name = "SpiceCompose"
include(":androidApp")
include(":composeApp")
