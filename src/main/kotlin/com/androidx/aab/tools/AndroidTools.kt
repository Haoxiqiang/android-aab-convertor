package com.androidx.aab.tools

import java.io.File

object AndroidTools {
    private val env = System.getenv()
    private val androidSDKHome = env["ANDROID_HOME"] ?: env["ANDROID_SDK"]
    private val androidNDKHome = env["ANDROID_NDK_HOME"] ?: env["ANDROID_NDK_ROOT"]

    // TODO auto install the latest version.
    enum class BuiltToolsVersions(val version: String) {
        V2401("24.0.1"),
        V2502("25.0.2"),
        V2601("26.0.1"),
        V2602("26.0.2"),
        V2603("26.0.3"),
        V2703("27.0.3"),
        V2802("28.0.2"),
        V2803("28.0.3"),
        V2900("29.0.0"),
        V2901("29.0.1"),
        V2902("29.0.2"),
        V2903("29.0.3"),
        V3000("30.0.0"),
        V3001("30.0.1"),
        V3002("30.0.2"),
        V3003("30.0.3"),
        V3100("31.0.0"),
        V3200("32.0.0"),
        V3300("33.0.0"),
        V3301("33.0.1"),
        V3302("33.0.2"),
        V3400("34.0.0"),
    }

    private fun queryVersions(): String {
        val versions = BuiltToolsVersions.values()
        versions.sortDescending()
        val version = versions.first { buildTools ->
            val file = File(
                testAAPT2(buildTools.version)
            )
            return@first file.exists()
        }
        return version.version
    }

    private fun testAAPT2(version: String): String {
        return "$androidSDKHome/build-tools/$version/aapt2"
    }

    fun getAAPT(version: String = queryVersions()): String {
        return "$androidSDKHome/build-tools/$version/aapt"
    }

    fun getAAPT2(version: String = queryVersions()): String {
        return "$androidSDKHome/build-tools/$version/aapt2"
    }

    fun getApkSigner(version: String = queryVersions()): String {
        return "$androidSDKHome/build-tools/$version/apksigner"
    }

    fun getZipAlign(version: String = queryVersions()): String {
        return "$androidSDKHome/build-tools/$version/zipalign"
    }
}