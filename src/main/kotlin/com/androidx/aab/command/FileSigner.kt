package com.androidx.aab.command

import com.android.apksig.ApkSigner
import com.android.apksig.DefaultApkSignerEngine
import com.androidx.aab.tools.SigningTools
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "sign",
    version = ["sign 1.0.0"],
    description = ["A APK/AAB File Signer."]
)
class FileSigner : Callable<Int> {

    @CommandLine.Option(names = ["-i", "--in"], description = ["Input file"], required = true)
    private lateinit var input: File

    @CommandLine.Option(names = ["-k", "--ks"], description = ["KeyStore file"], required = true)
    private lateinit var keyStorePath: File

    @CommandLine.Option(names = ["-ksp", "--ks-pass"], description = ["KeyStore password"], required = true)
    private lateinit var keyStorePass: String

    @CommandLine.Option(names = ["-ksa", "--ks-key-alias"], description = ["KeyStore alias name"], required = true)
    private lateinit var keyStoreAlias: String

    @CommandLine.Option(names = ["-kp", "--key-pass"], description = ["Key password"], required = true)
    private lateinit var keyPass: String

    override fun call(): Int {

        if (input.extension == "apk" || input.extension == "aab") {

            val output = File(input.parent, input.nameWithoutExtension + "_signed." + input.extension)
            val signingConfiguration = SigningTools.extractFromKeystore(
                keystorePath = keyStorePath.toPath(),
                keyAlias = keyStoreAlias,
                keystorePassword = keyStorePass,
                keyPassword = keyPass,
            )

            val signerConfig = listOf(signingConfiguration.signerConfig)
                .map { sc ->
                    DefaultApkSignerEngine.SignerConfig.Builder(
                        input.nameWithoutExtension,
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
                .setInputApk(input)
                .setOutputApk(output)
                .build()
                .sign()

        } else {
            println("not support ${input.absolutePath} format.")
            return -1
        }
        return 0
    }
}
