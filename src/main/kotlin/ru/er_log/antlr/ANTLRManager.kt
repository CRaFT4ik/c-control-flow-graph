package ru.er_log.antlr

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeListener
import org.antlr.v4.runtime.tree.ParseTreeWalker
import ru.er_log.antlr.gen.c.CLexer
import ru.er_log.antlr.gen.c.CParser


class ANTLRManager(input: String, private val listener: ParseTreeListener)
{
    private val lexer: CLexer           = CLexer(CharStreams.fromString(input))
    private val parser: CParser         = CParser(CommonTokenStream(lexer))
    private val tree: ParseTree         = parser.compilationUnit()
    private val walker: ParseTreeWalker = ParseTreeWalker()

    fun run() {
        walker.walk(listener, tree)
    }
}