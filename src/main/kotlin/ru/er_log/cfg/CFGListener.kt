package ru.er_log.cfg

import com.github.aakira.napier.Napier
import org.antlr.v4.runtime.tree.TerminalNode
import ru.er_log.antlr.gen.c.CBaseListener
import ru.er_log.antlr.gen.c.CParser


class CFGListener(private val graph: CFGraph) : CBaseListener()
{
    private var altCounter = 0; get() = field++

    /** Вход в функцию. */
    override fun enterFunctionDefinition(ctx: CParser.FunctionDefinitionContext) {
        val node: TerminalNode = ctx.declarator().directDeclarator().directDeclarator().Identifier()

        graph.add(CFGNodeFunction(ctx.altNumber, node.text))
        Napier.v("Enter in '${node.text}', state ${ctx.altNumber}")
    }

    /** Выход из функции. */
    override fun exitFunctionDefinition(ctx: CParser.FunctionDefinitionContext) {
        val node: TerminalNode = ctx.declarator().directDeclarator().directDeclarator().Identifier()

        Napier.v("Exit from '${node.text}', state ${ctx.altNumber}\n")
    }

    /** Вызов функции. */
    override fun enterFunctionCall(ctx: CParser.FunctionCallContext) {
        val node = ctx.postfixExpression().primaryExpression().Identifier()

        Napier.v("Calling ${node.text}, state ${ctx.altNumber}")
    }

    override fun enterSelectionStatement(ctx: CParser.SelectionStatementContext) {

        Napier.v("Enter in 'if/switch' statement, state ${ctx.altNumber}")
    }

    override fun exitSelectionStatement(ctx: CParser.SelectionStatementContext) {

        Napier.v("Exit from 'if/switch' statement, state ${ctx.altNumber}")
    }
}