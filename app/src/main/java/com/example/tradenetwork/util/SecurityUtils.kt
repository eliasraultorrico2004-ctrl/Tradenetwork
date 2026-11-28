package com.example.tradenetwork.util

import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object SecurityUtils {

    fun isDeviceRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
    }

    private fun checkRootMethod1(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootMethod2(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun checkRootMethod3(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val inReader = BufferedReader(InputStreamReader(process.inputStream))
            inReader.readLine() != null
        } catch (e: Exception) {
            false
        } finally {
            process?.destroy()
        }
    }

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.BOARD.lowercase().contains("nox")
                || Build.BOOTLOADER.lowercase().contains("unknown")
                || Build.HARDWARE.lowercase().contains("goldfish")
                || Build.HARDWARE.lowercase().contains("ranchu")
                || Build.PRODUCT.lowercase().contains("sdk")
                || Build.PRODUCT.lowercase().contains("google_sdk")
                || Build.PRODUCT.lowercase().contains("sdk_x86")
                || Build.PRODUCT.lowercase().contains("vbox86p"))
    }
}
