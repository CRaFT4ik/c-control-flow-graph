package ru.er_log

import com.github.aakira.napier.Napier
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ArgType.Choice
import kotlinx.cli.required
import ru.er_log.graph.cfg.CFGManager
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.system.exitProcess


class Parser(programName: String)
{
    private val parser = ArgParser(programName)

    val choice = Choice(listOf("image", "text"), toVariant = {})

    val input by parser.option(ArgType.String, shortName = "i", fullName = "input", description = "Input C file").required()
    val output by parser.option(ArgType.String, shortName = "o", fullName = "output", description = "Output file path with name")

    fun parse(args: Array<String>)
    {
        try {
            parser.parse(args)
        } catch (e: IllegalStateException) {
            Napier.i(e.message!!)
            exitProcess(0)
        }
    }
}

fun main(args: Array<String>) {
    initLogger()

    val parser = Parser("program")
    parser.parse(args)

    val inputData = readFrom(parser.input)
    val result = CFGManager(inputData).calculate()

    Napier.i(result.toGraph())

    val output = File(parser.output + ".png")
    result.toImage { streamIn, available -> writeStreamTo(output, streamIn, available) }

    exitProcess(0) // Because OkHttp doesn't close download connection (wtf?).
}

fun initLogger() {
    val antilog = JvmAntilog()
    Napier.base(antilog)
}

fun readFrom(input: String): String
        = File(input).readText()

fun writeTo(output: String, bytes: ByteArray)
        = File(output).writeBytes(bytes)

fun writeStreamTo(file: File, streamIn: InputStream, available: Long?) = try {
    val streamOut = file.outputStream()
    val buffer = ByteArray(1024 * 4)

    var total: Long = 0
    var count: Int
    while (streamIn.read(buffer).also { count = it } != -1) {
        total += count
        streamOut.write(buffer, 0, count)
    }

    streamIn.close()
    streamOut.close()
} catch (e: IOException) {
    Napier.e("IO exception: ${e.message}")
}