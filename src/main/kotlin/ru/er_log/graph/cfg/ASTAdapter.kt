package ru.er_log.graph.cfg

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.Interval
import ru.er_log.antlr.gen.c.CBaseListener
import ru.er_log.antlr.gen.c.CParser
import ru.er_log.graph.cfg.nodes.CFGNode
import ru.er_log.graph.cfg.nodes.linear.*
import ru.er_log.graph.cfg.nodes.nonlinear.*
import java.util.*


class ASTAdapter(private val graph: CFGraph) : CBaseListener()
{
    private val stack: Stack<CFGNode> = Stack()

    private val ParserRuleContext.name: String
        get() {
            val interval = Interval(this.start.startIndex, this.stop.stopIndex)
            return this.start.inputStream.getText(interval).trim()
        }

    /** Начало и конец парсинга. */

    override fun enterCompilationUnit(ctx: CParser.CompilationUnitContext) {
        graph.start()
    }

    override fun exitCompilationUnit(ctx: CParser.CompilationUnitContext) {
        graph.finish()
    }

    /** Определение функции. */

    override fun enterFunctionDefinition(ctx: CParser.FunctionDefinitionContext) {
        val node = CFGNodeFunction(ctx.altNumber, stack.size, ctx.functionDefinitionName().name)
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
        val expression = ctx.children.filterIsInstance<CParser.ExpressionContext>().first()
        val node = CFGNodeIfStatement(ctx.altNumber, stack.size, expression.name)
        graph.enter(stack.push(node))
    }

    override fun exitIfStatement(ctx: CParser.IfStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция ELSE IF. */

    override fun enterElseIfStatement(ctx: CParser.ElseIfStatementContext) {
        val expression = ctx.children.filterIsInstance<CParser.ExpressionContext>().first()
        val node = CFGNodeElseIfStatement(ctx.altNumber, stack.size, expression.name)
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
        /* Находим следующее: for (conditionSteps) == for (stepInitialValue; stepCondition; приращение) */
        val conditionSteps = ctx.children.filterIsInstance<CParser.ForConditionStepsContext>().first()
        val stepInitialValue = conditionSteps.children.filterIsInstance<CParser.ForStepInitialValueContext>().firstOrNull()
        val stepCondition = conditionSteps.children.filterIsInstance<CParser.ForStepConditionContext>().firstOrNull()

        /** Есть ли необходимость вставлять код инициализации итератора цикла перед
          * самим [CFGNodeForStatement] в графе? Если нет, следующий блок кода не нужен. */
        stepInitialValue?.let {
            _enterForStepInitialValue(it)
            _exitForStepInitialValue(it)
        }

        val node = CFGNodeForStatement(CFGNodeForStatement.NodeType.CONDITION, ctx.altNumber, stack.size, stepCondition?.name ?: "true")
        graph.enter(stack.push(node))
    }

    override fun exitForStatement(ctx: CParser.ForStatementContext) {
        /* Находим следующее: for (conditionSteps) == for (начальное_значение; stepCondition; приращение) */
        val conditionSteps = ctx.children.filterIsInstance<CParser.ForConditionStepsContext>().first()
        val stepIncrement = conditionSteps.children.filterIsInstance<CParser.ForStepIncrementContext>().firstOrNull()

        stepIncrement?.let {
            _enterForStepIncrement(it)
            _exitForStepIncrement(it)
        }

        graph.close(stack.pop())
    }

    private fun _enterForStepInitialValue(ctx: CParser.ForStepInitialValueContext) {
        val node = CFGNodeForStatement(CFGNodeForStatement.NodeType.INITIAL, ctx.altNumber, stack.size, ctx.name)
        graph.enter(stack.push(node))
    }

    private fun _exitForStepInitialValue(ctx: CParser.ForStepInitialValueContext) {
        graph.close(stack.pop())
    }

    private fun _enterForStepIncrement(ctx: CParser.ForStepIncrementContext) {
        val node = CFGNodeForStatement(CFGNodeForStatement.NodeType.INCREMENT, ctx.altNumber, stack.size, ctx.name)
        graph.enter(stack.push(node))
    }

    private fun _exitForStepIncrement(ctx: CParser.ForStepIncrementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция WHILE. */

    override fun enterWhileStatement(ctx: CParser.WhileStatementContext) {
        val expression = ctx.children.filterIsInstance<CParser.ExpressionContext>().first()
        val node = CFGNodeWhileStatement(ctx.altNumber, stack.size, expression.name)
        graph.enter(stack.push(node))
    }

    override fun exitWhileStatement(ctx: CParser.WhileStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция DO WHILE. */

    override fun enterDoWhileStatement(ctx: CParser.DoWhileStatementContext) {
        val expression = ctx.children.filterIsInstance<CParser.ExpressionContext>().first()
        val node = CFGNodeDoWhileStatement(ctx.altNumber, stack.size, expression.name)
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
        val node = CFGNodeFunctionCall(ctx.altNumber, stack.size, ctx.name)
        graph.enter(stack.push(node))
    }

    override fun exitFunctionCall(ctx: CParser.FunctionCallContext) {
        graph.close(stack.pop())
    }

    /** Метка. */

    override fun enterLabeledStatement(ctx: CParser.LabeledStatementContext) {
        val node = CFGNodeLabel(ctx.altNumber, stack.size, ctx.Identifier().text)
        graph.enter(stack.push(node))
    }


    override fun exitLabeledStatement(ctx: CParser.LabeledStatementContext) {
        graph.close(stack.pop())
    }

    /** Инструкция GOTO. */

    override fun enterGotoStatement(ctx: CParser.GotoStatementContext) {
        val node = CFGNodeGotoStatement(ctx.altNumber, stack.size, ctx.Identifier().text)
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