package ru.er_log

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import java.io.File
import kotlin.system.exitProcess


class Parser(val programName: String)
{
    private val parser = ArgParser(programName)

    val input by parser.option(ArgType.String, shortName = "i", description = "Input C file").required()
    val output by parser.option(ArgType.String, shortName = "o", description = "Output file path with name").required()

    fun parse(args: Array<String>)
    {
        try {
            parser.parse(args)
        } catch (e: IllegalStateException) {
            println(e.message)
            exitProcess(0)
        }
    }
}

fun main(args: Array<String>) {
    val parser = Parser("program")
    parser.parse(args)

    val inputData = readFrom(parser.input)
    val result = CFG(inputData).calculate()

    writeTo(parser.output, result.toImage())
}

fun readFrom(input: String): String
        = File(input).readText()

fun writeTo(output: String, bytes: ByteArray)
        = File(output).writeBytes(bytes)
