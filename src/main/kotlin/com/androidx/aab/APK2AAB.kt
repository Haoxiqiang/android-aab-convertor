package com.androidx.aab

import com.android.apksig.ApkSigner
import com.android.apksig.DefaultApkSignerEngine
import com.android.apksig.apk.ApkFormatException
import com.android.bundle.Config
import com.android.tools.build.bundletool.commands.BuildBundleCommand
import com.androidx.aab.tools.AndroidTools
import com.androidx.aab.tools.SigningTools
import com.google.common.collect.ImmutableList
import java.io.*
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SignatureException
import java.util.*
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


object APK2AAB {

    private const val BUFFER_SIZE = 1024 * 2

    //default values AGP uses
    private val mFilesNotToCompress = listOf(
        "**.3[gG]2",
        "**.3[gG][pP]",
        "**.3[gG][pP][pP]",
        "**.3[gG][pP][pP]2",
        "**.[aA][aA][cC]",
        "**.[aA][mM][rR]",
        "**.[aA][wW][bB]",
        "**.[gG][iI][fF]",
        "**.[iI][mM][yY]",
        "**.[jJ][eE][tT]",
        "**.[jJ][pP][eE][gG]",
        "**.[jJ][pP][gG]",
        "**.[mM]4[aA]",
        "**.[mM]4[vV]",
        "**.[mM][iI][dD]",
        "**.[mM][iI][dD][iI]",
        "**.[mM][kK][vV]",
        "**.[mM][pP]2",
        "**.[mM][pP]3",
        "**.[mM][pP]4",
        "**.[mM][pP][eE][gG]",
        "**.[mM][pP][gG]",
        "**.[oO][gG][gG]",
        "**.[oO][pP][uU][sS]",
        "**.[pP][nN][gG]",
        "**.[rR][tT][tT][tT][lL]",
        "**.[sS][mM][fF]",
        "**.[tT][fF][lL][iI][tT][eE]",
        "**.[wW][aA][vV]",
        "**.[wW][eE][bB][mM]",
        "**.[wW][eE][bB][pP]",
        "**.[wW][mM][aA]",
        "**.[wW][mM][vV]",
        "**.[xX][mM][fF]"
    )

    fun convert(input: File, output: File) {

        println("Starting apk to aab")

        val tempDir = File(input.parent, "${input.nameWithoutExtension}-out")
        if (!tempDir.isDirectory) {
            tempDir.deleteRecursively()
        }
        tempDir.mkdirs()

        val protoZip = createProtoFormatZip(tempDir, input)
        val baseZip = createBaseZip(tempDir, protoZip)
        val noSignAABFile = buildAABFile(tempDir, baseZip)
        val signedAABFile = signAABFile(tempDir, noSignAABFile)
        signedAABFile.copyTo(output)

        tempDir.deleteRecursively()
    }

    // proto convert
    @Throws(Exception::class)
    private fun createProtoFormatZip(dir: File, input: File): File {
        println("Creating proto formatted zip")

        val outFile = File(dir, "proto.zip")
        if (outFile.exists()) {
            outFile.delete()
        }
        outFile.createNewFile()

        val processBuilder = ProcessBuilder()
        val stringWriter = StringWriter()

        val args: MutableList<String> = ArrayList()
        args.add(AndroidTools.getAAPT2())
        args.add("convert")
        args.add("-o")
        args.add(outFile.absolutePath)
        args.add("--output-format")
        args.add("proto")
        args.add("--keep-raw-values")
        args.add(input.absolutePath)

        processBuilder.command(args)
        val process = processBuilder.start()
        val scanner = Scanner(process.errorStream)
        var hasError = false
        while (scanner.hasNextLine()) {
            hasError = true
            val log: String = scanner.nextLine()
            println(log)
            stringWriter.append(log)
            stringWriter.append(System.lineSeparator())
        }
        process.waitFor()
        if (hasError) {
            throw Exception(stringWriter.toString())
        }
        return outFile
    }

    @Throws(IOException::class)
    private fun createBaseZip(dir: File, protoZip: File): File {

        val outFile = File(dir, "base.zip")
        if (outFile.exists()) {
            outFile.delete()
        }

        println("Creating base.zip")
        outFile.createNewFile()

        val protoInputStream = FileInputStream(protoZip)
        val zipInputStream = ZipInputStream(protoInputStream)
        val zipFile = ZipFile(protoZip)

        val baseOutputStream = FileOutputStream(outFile)
        val zipOutputStream = ZipOutputStream(baseOutputStream)

        val fds = listOf(protoInputStream, baseOutputStream, zipInputStream, zipInputStream, zipFile)
        do {
            val entry: ZipEntry = zipInputStream.nextEntry ?: break
            val zipEntry = if (entry.name.startsWith("assets")) {
                // the META-INF folder may contain non-signature-related resources
                // as well, so we check if the entry doesn't point to a signature
                // file before adding it
                ZipEntry(entry.name)
            } else if (
                entry.name.endsWith(".dex") ||
                entry.name.startsWith("classes")
            ) {
                ZipEntry("dex" + File.separator + entry.name)
            } else if (entry.name == "AndroidManifest.xml") {
                ZipEntry("manifest" + File.separator + entry.name)
            } else if (
                entry.name.startsWith("res") ||
                entry.name.startsWith("lib")
            ) {
                ZipEntry(entry.name)
            } else if (entry.name == "resources.pb") {
                ZipEntry(entry.name)
            } else {
                ZipEntry("root" + File.separator + entry.name)
            }

            println("base.zip => from ${entry.name} to ${zipEntry.name}")

            zipFile.getInputStream(ZipEntry(entry.name))
                .use { inputStream ->
                    zipOutputStream.putNextEntry(zipEntry)
                    inputStream.copyTo(zipOutputStream, BUFFER_SIZE)
                }
        } while (true)

        // must call finish.
        zipOutputStream.finish()

        fds.forEach { closeable ->
            try {
                closeable.close()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }

        return outFile
    }

    private fun buildAABFile(dir: File, baseZip: File): File {
        println("Creating aab")

        val outFile = File(dir, "non-signed.aab")
        if (outFile.exists()) {
            outFile.delete()
        }
        outFile.createNewFile()

        println(baseZip.absolutePath)
        println(outFile.absolutePath)

        val compression = Config.Compression.newBuilder()
            .addAllUncompressedGlob(mFilesNotToCompress)
            .build()

        val bundleConfig = Config.BundleConfig
            .newBuilder()
            .mergeCompression(compression)
            .build()

        val command = BuildBundleCommand.builder()
            .setModulesPaths(ImmutableList.of(baseZip.toPath()))
            .setOutputPath(outFile.toPath())
            .setBundleConfig(bundleConfig)
            .setOverwriteOutput(true)
            .build()

        command.execute()
        println("Successfully converted Apk to AAB")

        return outFile
    }

    @Throws(
        ApkFormatException::class,
        IOException::class,
        NoSuchAlgorithmException::class,
        SignatureException::class,
        InvalidKeyException::class
    )
    fun signAABFile(dir: File, aab: File): File {

        val outFile = File(dir, "signed.aab")
        if (outFile.exists()) {
            outFile.delete()
        }
        outFile.createNewFile()

        val keystoreFile = File(dir, "keystore.jks")
        val signingConfiguration = SigningTools.extractFromKeystoreJKSFile(keystoreFile)
        val signerConfig = listOf(signingConfiguration.signerConfig).map { sc ->
            DefaultApkSignerEngine.SignerConfig.Builder(
                keystoreFile.nameWithoutExtension,
                sc.privateKey,
                sc.certificates
            ).build()
        }
        val apkSignerEngine = DefaultApkSignerEngine
            .Builder(signerConfig, 1)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .build()

        ApkSigner.Builder(apkSignerEngine)
            .setInputApk(aab)
            .setOutputApk(outFile)
            .build()
            .sign()

        return outFile
    }
}