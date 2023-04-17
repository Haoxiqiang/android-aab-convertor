package com.androidx.aab

import java.io.File
import kotlin.system.exitProcess


class AABTool {

    @Throws(Exception::class)
    fun call(inputs: Map<String, String>): Int {

        //val jarLocation: String = AABTool::class.java
        //    .protectionDomain.codeSource.location.path
        //println(jarLocation)

        val input: File? = inputs["-i"]?.let { File(it) }
        val output: File? = inputs["-o"]?.let { File(it) }

        if (input == null) {
            println("Input file not found. use -i file's path")
            return -1
        }

        if (output == null) {
            println("Output file not found. use -i file's path")
            return -1
        }

        if (input.extension == output.extension) {
            input.copyTo(output)
            return 0
        }

        if (input.extension == "apk" && output.extension == "aab") {
            APK2AAB.convert(input, output)
        } else if (input.extension == "aab" && output.extension == "apks") {
            AAB2APK.convert(input, output)
        } else {
            println("input:$input output:$output not support.")
        }
        return 0
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println(args.joinToString())
            val inputs = args.associate { arg ->
                val pair = arg.split("=")
                Pair(pair[0], pair[1])
            }
            val aabTool = AABTool()
            val exitCode = aabTool.call(inputs)
            exitProcess(exitCode)
        }
    }
}