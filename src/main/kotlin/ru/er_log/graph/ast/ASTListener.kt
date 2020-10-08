package ru.er_log.graph.ast

import org.antlr.v4.runtime.tree.TerminalNode
import ru.er_log.antlr.gen.c.CBaseListener
import ru.er_log.antlr.gen.c.CParser
import ru.er_log.graph.cfg.*


class ASTListener(private val graph: CFGraph) : CBaseListener()
{
    /** Начало и конец парсинга. */

    override fun enterCompilationUnit(ctx: CParser.CompilationUnitContext) {
        graph.start()
        //graph.enter(ASTNodeFunction(ctx.altNumber, "entry point"))
    }

    override fun exitCompilationUnit(ctx: CParser.CompilationUnitContext) {
        graph.finish()
        //graph.close(ctx.altNumber)
    }

    /** Определение функции. */

    override fun enterFunctionDefinition(ctx: CParser.FunctionDefinitionContext) {
        val node: TerminalNode = ctx.declarator().directDeclarator().directDeclarator().Identifier()
        graph.enter(CFGNodeFunction(ctx.altNumber, node.text + "()"))
    }

    override fun exitFunctionDefinition(ctx: CParser.FunctionDefinitionContext) {
        graph.close(ctx.altNumber)
    }

    /** Вызов функции. */

    override fun enterFunctionCall(ctx: CParser.FunctionCallContext) {
        val node = ctx.postfixExpression().primaryExpression().Identifier()
        graph.enter(CFGNodeFunctionCall(ctx.altNumber, node.text + "()"))
    }

    override fun exitFunctionCall(ctx: CParser.FunctionCallContext) {
        graph.close(ctx.altNumber)
    }

    /** Инструкция IF. */

    override fun enterIfStatement(ctx: CParser.IfStatementContext) {
        graph.enter(CFGNodeIfStatement(ctx.altNumber))
    }

    override fun exitIfStatement(ctx: CParser.IfStatementContext) {
        graph.close(ctx.altNumber)
    }

    /** Инструкция ELSE IF. */

    override fun enterElseIfStatement(ctx: CParser.ElseIfStatementContext) {
        graph.enter(CFGNodeElseIfStatement(ctx.altNumber))
    }

    override fun exitElseIfStatement(ctx: CParser.ElseIfStatementContext) {
        graph.close(ctx.altNumber)
    }

    /** Инструкция ELSE. */

    override fun enterElseStatement(ctx: CParser.ElseStatementContext) {
        graph.enter(CFGNodeElseStatement(ctx.altNumber))
    }

    override fun exitElseStatement(ctx: CParser.ElseStatementContext) {
        graph.close(ctx.altNumber)
    }

    /** Инструкция FOR. */

    override fun enterForStatement(ctx: CParser.ForStatementContext) {
        graph.enter(CFGNodeForStatement(ctx.altNumber))
    }

    override fun exitForStatement(ctx: CParser.ForStatementContext) {
        graph.close(ctx.altNumber)
    }

    /** Инструкция WHILE. */

    override fun enterWhileStatement(ctx: CParser.WhileStatementContext) {
        graph.enter(CFGNodeWhileStatement(ctx.altNumber))
    }

    override fun exitWhileStatement(ctx: CParser.WhileStatementContext) {
        graph.close(ctx.altNumber)
    }

    /** Инструкция DO WHILE. */

    override fun enterDoWhileStatement(ctx: CParser.DoWhileStatementContext) {
        graph.enter(CFGNodeDoWhileStatement(ctx.altNumber))
    }

    override fun exitDoWhileStatement(ctx: CParser.DoWhileStatementContext) {
        graph.close(ctx.altNumber)
    }

    /** Инструкции перехода. */

    override fun enterJumpStatement(ctx: CParser.JumpStatementContext) {
        graph.enter(CFGNodeJumpStatement(ctx.altNumber))
    }

    override fun exitJumpStatement(ctx: CParser.JumpStatementContext) {
        graph.close(ctx.altNumber)
    }
}