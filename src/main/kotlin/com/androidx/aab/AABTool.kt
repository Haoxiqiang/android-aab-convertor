package com.androidx.aab

import com.androidx.aab.command.FileConverter
import com.androidx.aab.command.FileSigner
import picocli.CommandLine


@CommandLine.Command(
    name = "aabtools",
    mixinStandardHelpOptions = true,
    subcommands = [CommandLine.HelpCommand::class],
    version = ["aabtools 1.0.0"],
    description = ["A APK/AAB File Converter."]
)
class AABTool {

    companion object {

         const val KEYSTORE_PASSWORD = "KP20230417"
         const val KEY_ALIAS = "google"
         const val KEY_PASSWORD = "KP20230417"

        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine(AABTool())
                .addSubcommand("convert", FileConverter())
                .addSubcommand("sign", FileSigner())
                .execute(*args)
        }
    }
}
