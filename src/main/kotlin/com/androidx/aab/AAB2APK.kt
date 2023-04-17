package com.androidx.aab

import com.android.apksig.DefaultApkSignerEngine
import com.android.tools.build.bundletool.androidtools.Aapt2Command
import com.android.tools.build.bundletool.commands.BuildApksCommand
import com.android.tools.build.bundletool.model.SigningConfiguration
import com.androidx.aab.tools.AndroidTools
import com.androidx.aab.tools.SigningTools
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.io.path.Path


object AAB2APK {

    fun convert(input: File, output: File) {

        println("Starting apk to AAB")

        val tempDir = File(input.parent, "${input.nameWithoutExtension}-out")
        if (!tempDir.isDirectory) {
            tempDir.deleteRecursively()
        }
        tempDir.mkdirs()

        val aapt2Path = AndroidTools.getAAPT2()
        val outputStream = ByteArrayOutputStream()
        val aapt2Command: Aapt2Command = Aapt2Command.createFromExecutablePath(Path(aapt2Path))
        val builder = BuildApksCommand.builder()
            .setAapt2Command(aapt2Command)
            .setBundlePath(input.toPath())
            .setOutputFile(output.toPath())
            .setOverwriteOutput(true)
            .setApkBuildMode(BuildApksCommand.ApkBuildMode.UNIVERSAL)
            .setOutputPrintStream(PrintStream(outputStream))

        val keystoreFile = File(tempDir, "keystore.jks")
        val signingConfiguration = SigningTools.extractFromKeystoreJKSFile(keystoreFile)

        builder.setSigningConfiguration(signingConfiguration)
        builder.build().execute()

        println(outputStream.toString())
        println("Successfully converted AAB to Apk")

        tempDir.deleteRecursively()
    }
}