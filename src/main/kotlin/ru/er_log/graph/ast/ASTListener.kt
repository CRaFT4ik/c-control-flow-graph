package ru.er_log.graph.ast

import org.antlr.v4.runtime.tree.TerminalNode
import ru.er_log.antlr.gen.c.CBaseListener
import ru.er_log.antlr.gen.c.CParser
import ru.er_log.graph.cfg.*
import java.util.*


class ASTListener(private val graph: CFGraph) : CBaseListener()
{
    private val stack: Stack<CFGNode> = Stack()

    /** Начало и конец парсинга. */

    override fun enterCompilationUnit(ctx: CParser.CompilationUnitContext) {
        graph.start()
    }

    override fun exitCompilationUnit(ctx: CParser.CompilationUnitContext) {
        graph.finish()
    }

    /** Определение функции. */

    override fun enterFunctionDefinition(ctx: CParser.FunctionDefinitionContext) {
        val node: TerminalNode = ctx.declarator().directDeclarator().directDeclarator().Identifier()
        graph.enter(stack.push(CFGNodeFunction(ctx.altNumber, node.text + "(...)")))
    }

    override fun exitFunctionDefinition(ctx: CParser.FunctionDefinitionContext) {
        graph.close(stack.pop())
    }

    /**
     * Инструкции выбора.
     */

    /** Инструкция IF. */

    override fun enterIfStatement(ctx: CParser.IfStatementContext) {
        graph.enter(stack.push(CFGNodeIfStatement(ctx.altNumber)))
    }

    override fun exitIfStatement(ctx: CParser.IfStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция ELSE IF. */

    override fun enterElseIfStatement(ctx: CParser.ElseIfStatementContext) {
        graph.enter(stack.push(CFGNodeElseIfStatement(ctx.altNumber)))
    }

    override fun exitElseIfStatement(ctx: CParser.ElseIfStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция ELSE. */

    override fun enterElseStatement(ctx: CParser.ElseStatementContext) {
        graph.enter(stack.push(CFGNodeElseStatement(ctx.altNumber)))
    }

    override fun exitElseStatement(ctx: CParser.ElseStatementContext) {
        graph.close(stack.pop())
    }

    /**
     * Итерационные инструкции.
     */

    /** Инструкция FOR. */

    override fun enterForStatement(ctx: CParser.ForStatementContext) {
        graph.enter(stack.push(CFGNodeForStatement(ctx.altNumber)))
    }

    override fun exitForStatement(ctx: CParser.ForStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция WHILE. */

    override fun enterWhileStatement(ctx: CParser.WhileStatementContext) {
        graph.enter(stack.push(CFGNodeWhileStatement(ctx.altNumber)))
    }

    override fun exitWhileStatement(ctx: CParser.WhileStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция DO WHILE. */

    override fun enterDoWhileStatement(ctx: CParser.DoWhileStatementContext) {
        graph.enter(stack.push(CFGNodeDoWhileStatement(ctx.altNumber)))
    }

    override fun exitDoWhileStatement(ctx: CParser.DoWhileStatementContext) {
        graph.close(stack.pop())
    }

    /**
     * Инструкции перехода.
     */

    /** Вызов функции. */

    override fun enterFunctionCall(ctx: CParser.FunctionCallContext) {
        val node = ctx.postfixExpression().primaryExpression().Identifier()
        graph.enter(stack.push(CFGNodeFunctionCall(ctx.altNumber, node.text + "(...)")))
    }

    override fun exitFunctionCall(ctx: CParser.FunctionCallContext) {
        graph.close(stack.pop())
    }

    /** Инструкция GOTO. */

    override fun enterGotoStatement(ctx: CParser.GotoStatementContext) {
        graph.enter(stack.push(CFGNodeGotoStatement(ctx.altNumber)))
    }

    override fun exitGotoStatement(ctx: CParser.GotoStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция CONTINUE. */

    override fun enterContninueStatement(ctx: CParser.ContninueStatementContext) {
        graph.enter(stack.push(CFGNodeContinueStatement(ctx.altNumber)))
    }

    override fun exitContninueStatement(ctx: CParser.ContninueStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция BREAK. */

    override fun enterBreakStatement(ctx: CParser.BreakStatementContext) {
        graph.enter(stack.push(CFGNodeBreakStatement(ctx.altNumber)))
    }

    override fun exitBreakStatement(ctx: CParser.BreakStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция RETURN. */

    override fun enterReturnStatement(ctx: CParser.ReturnStatementContext) {
        graph.enter(stack.push(CFGNodeReturnStatement(ctx.altNumber)))
    }

    override fun exitReturnStatement(ctx: CParser.ReturnStatementContext) {
        graph.close(stack.pop())
    }
}