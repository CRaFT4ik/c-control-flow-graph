package ru.er_log.cfg

import org.antlr.v4.runtime.tree.TerminalNode
import ru.er_log.antlr.gen.c.CBaseListener
import ru.er_log.antlr.gen.c.CParser


class CFGListener(private val graph: CFGraph) : CBaseListener()
{
    /** Вход в функцию. */
    override fun enterFunctionDefinition(ctx: CParser.FunctionDefinitionContext) {
        val node: TerminalNode = ctx.declarator().directDeclarator().directDeclarator().Identifier()
        graph.enter(CFGNodeFunction(ctx.altNumber, node.text))
    }

    /** Выход из функции. */
    override fun exitFunctionDefinition(ctx: CParser.FunctionDefinitionContext) {
        val node: TerminalNode = ctx.declarator().directDeclarator().directDeclarator().Identifier()
        graph.close(CFGNodeFunction(ctx.altNumber, node.text))
    }

    /** Вызов функции. */
    override fun enterFunctionCall(ctx: CParser.FunctionCallContext) {
        val node = ctx.postfixExpression().primaryExpression().Identifier()
        graph.enter(CFGNodeFunctionCall(ctx.altNumber, node.text))
    }

    override fun enterIfStatement(ctx: CParser.IfStatementContext) {
        graph.enter(CFGNodeIfStatement(ctx.altNumber))
    }

    override fun exitIfStatement(ctx: CParser.IfStatementContext) {
        graph.close(CFGNodeIfStatement(ctx.altNumber))
    }

    override fun enterElseStatement(ctx: CParser.ElseStatementContext) {
        graph.enter(CFGNodeElseStatement(ctx.altNumber))
    }

    override fun exitElseStatement(ctx: CParser.ElseStatementContext) {
        graph.close(CFGNodeElseStatement(ctx.altNumber))
    }
}