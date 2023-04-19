package com.androidx.aab.command

import com.androidx.aab.AAB2APK
import com.androidx.aab.APK2AAB
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "convert",
    version = ["convert 1.0.0"],
    description = ["A APK/AAB File Converter."]
)
class FileConverter : Callable<Int> {
    @CommandLine.Option(names = ["-i", "--in"], description = ["Input file"])
    private var input: File? = null

    @CommandLine.Option(names = ["-o", "--out"], description = ["Output file"])
    private var output: File? = null

    override fun call(): Int {

        if (input == null) {
            println("Input file not found. use -i file's path")
            return -1
        }

        if (output == null) {
            println("Output file not found. use -i file's path")
            return -1
        }

        val i = input!!
        val o = output!!

        if (i.extension == o.extension) {
            i.copyTo(o)
            return 0
        }

        if (i.extension == "apk" && o.extension == "aab") {
            APK2AAB.convert(i, o)
        } else if (i.extension == "aab" && o.extension == "apk") {
            AAB2APK.convert(i, o)
        } else {
            println("input:$i output:$output not support.")
        }

        return 0
    }
}
