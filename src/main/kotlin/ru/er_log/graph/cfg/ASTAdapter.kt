package ru.er_log.graph.cfg

import org.antlr.v4.runtime.tree.TerminalNode
import ru.er_log.antlr.gen.c.CBaseListener
import ru.er_log.antlr.gen.c.CParser
import ru.er_log.graph.cfg.nodes.CFGNode
import ru.er_log.graph.cfg.nodes.linear.*
import ru.er_log.graph.cfg.nodes.nonlinear.*
import java.util.*

class ASTAdapter(private val graph: CFGraph) : CBaseListener()
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
        val origin: TerminalNode = ctx.declarator().directDeclarator().directDeclarator().Identifier()
        val node = CFGNodeFunction(ctx.altNumber, stack.size, origin.text + "(...)")
        graph.enter(stack.push(node))
    }

    override fun exitFunctionDefinition(ctx: CParser.FunctionDefinitionContext) {
        graph.close(stack.pop())
    }

    /**
     * Инструкции выбора.
     */

    /** Инструкция IF. */

    override fun enterIfStatement(ctx: CParser.IfStatementContext) {
        val node = CFGNodeIfStatement(ctx.altNumber, stack.size)
        graph.enter(stack.push(node))
    }

    override fun exitIfStatement(ctx: CParser.IfStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция ELSE IF. */

    override fun enterElseIfStatement(ctx: CParser.ElseIfStatementContext) {
        val node = CFGNodeElseIfStatement(ctx.altNumber, stack.size)
        graph.enter(stack.push(node))
    }

    override fun exitElseIfStatement(ctx: CParser.ElseIfStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция ELSE. */

    override fun enterElseStatement(ctx: CParser.ElseStatementContext) {
        val node = CFGNodeElseStatement(ctx.altNumber, stack.size)
        graph.enter(stack.push(node))
    }

    override fun exitElseStatement(ctx: CParser.ElseStatementContext) {
        graph.close(stack.pop())
    }

    /**
     * Итерационные инструкции.
     */

    /** Инструкция FOR. */

    override fun enterForStatement(ctx: CParser.ForStatementContext) {
        val node = CFGNodeForStatement(ctx.altNumber, stack.size)
        graph.enter(stack.push(node))
    }

    override fun exitForStatement(ctx: CParser.ForStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция WHILE. */

    override fun enterWhileStatement(ctx: CParser.WhileStatementContext) {
        val node = CFGNodeWhileStatement(ctx.altNumber, stack.size)
        graph.enter(stack.push(node))
    }

    override fun exitWhileStatement(ctx: CParser.WhileStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция DO WHILE. */

    override fun enterDoWhileStatement(ctx: CParser.DoWhileStatementContext) {
        val node = CFGNodeDoWhileStatement(ctx.altNumber, stack.size)
        graph.enter(stack.push(node))
    }

    override fun exitDoWhileStatement(ctx: CParser.DoWhileStatementContext) {
        graph.close(stack.pop())
    }

    /**
     * Инструкции перехода.
     */

    /** Вызов функции. */

    override fun enterFunctionCall(ctx: CParser.FunctionCallContext) {
        val origin = ctx.postfixExpression().primaryExpression().Identifier()
        val node = CFGNodeFunctionCall(ctx.altNumber, stack.size, origin.text + "(...)")
        graph.enter(stack.push(node))
    }

    override fun exitFunctionCall(ctx: CParser.FunctionCallContext) {
        graph.close(stack.pop())
    }

    /** Инструкция GOTO. */

    override fun enterGotoStatement(ctx: CParser.GotoStatementContext) {
        val node = CFGNodeGotoStatement(ctx.altNumber, stack.size)
        graph.enter(stack.push(node))
    }

    override fun exitGotoStatement(ctx: CParser.GotoStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция CONTINUE. */

    override fun enterContninueStatement(ctx: CParser.ContninueStatementContext) {
        val node = CFGNodeContinueStatement(ctx.altNumber, stack.size)
        graph.enter(stack.push(node))
    }

    override fun exitContninueStatement(ctx: CParser.ContninueStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция BREAK. */

    override fun enterBreakStatement(ctx: CParser.BreakStatementContext) {
        val node = CFGNodeBreakStatement(ctx.altNumber, stack.size)
        graph.enter(stack.push(node))
    }

    override fun exitBreakStatement(ctx: CParser.BreakStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция RETURN. */

    override fun enterReturnStatement(ctx: CParser.ReturnStatementContext) {
        val node = CFGNodeReturnStatement(ctx.altNumber, stack.size)
        graph.enter(stack.push(node))
    }

    override fun exitReturnStatement(ctx: CParser.ReturnStatementContext) {
        graph.close(stack.pop())
    }
}