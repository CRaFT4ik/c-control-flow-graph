package ru.er_log.antlr

import org.antlr.v4.runtime.tree.TerminalNode
import ru.er_log.antlr.gen.c.CBaseListener
import ru.er_log.antlr.gen.c.CParser


class CListener : CBaseListener()
{
    override fun enterFunctionDefinition(ctx: CParser.FunctionDefinitionContext?) {
        val node: TerminalNode? = ctx?.declarator()?.directDeclarator()?.directDeclarator()?.Identifier()
        println(node?.text)
    }

}