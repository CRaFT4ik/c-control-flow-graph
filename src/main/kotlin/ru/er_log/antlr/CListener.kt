package ru.er_log.antlr

import com.github.aakira.napier.Napier
import org.antlr.v4.runtime.tree.TerminalNode
import ru.er_log.antlr.gen.c.CBaseListener
import ru.er_log.antlr.gen.c.CParser


class CListener : CBaseListener()
{
    /** Вход в функцию. */
    override fun enterFunctionDefinition(ctx: CParser.FunctionDefinitionContext) {
        val node: TerminalNode = ctx.declarator().directDeclarator().directDeclarator().Identifier()
        Napier.v("Enter in '${node.text}'")
    }

    /** Выход из функции. */
    override fun exitFunctionDefinition(ctx: CParser.FunctionDefinitionContext) {
        val node: TerminalNode = ctx.declarator().directDeclarator().directDeclarator().Identifier()
        Napier.v("Exit from '${node.text}'\n")
    }

    /** Вызов функции. */
    override fun enterFunctionCall(ctx: CParser.FunctionCallContext) {
        val node = ctx.postfixExpression().primaryExpression().Identifier()
        Napier.v("Calling ${node.text}")
    }
}