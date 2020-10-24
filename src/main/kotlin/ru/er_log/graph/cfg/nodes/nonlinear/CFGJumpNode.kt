package ru.er_log.graph.cfg.nodes.nonlinear

import ru.er_log.graph.LinkStyle
import ru.er_log.graph.NodeStyle
import ru.er_log.graph.StyleCatalogue
import ru.er_log.graph.cfg.nodes.CFGLink
import ru.er_log.graph.cfg.nodes.CFGNode
import ru.er_log.graph.cfg.nodes.CFGNonBodyNode
import ru.er_log.graph.cfg.nodes.linear.CFGIterationNode
import ru.er_log.graph.cfg.nodes.linear.CFGNodeFunction

abstract class CFGJumpNode(
    context: Int,
    deepness: Int,
    title: String = "jump statement",
    style: NodeStyle = StyleCatalogue.NodeStyles.jump
) : CFGNonBodyNode(context, deepness, title, style)
{
    override fun linkable(to: CFGNode): Boolean = when(to) {
        is CFGNodeFunction -> true
        is CFGIterationNode -> true
        else -> false
    }

    /** Передаем все узлы, привязанные к данному, узлу наследнику. */
    override fun link(other: CFGNode, defStyle: LinkStyle?, vararg type: CFGLink.LinkType) {
        if (!linkable(other)) { return }

        while(this.linked.size > 0) {
            val link = this.linked.first()
            link.to.unlink(this)
            link.to.link(other, link.style, CFGLink.LinkType.NONLINEAR)
        }
    }
}

data class CFGNodeGotoStatement(
    override val context: Int,
    override val deepness: Int,
    override val title: String = "goto statement"
) : CFGJumpNode(context, deepness, title)
{
    override fun linkable(to: CFGNode): Boolean = to is CFGNodeFunction
}

data class CFGNodeReturnStatement(
    override val context: Int,
    override val deepness: Int,
    override val title: String = "return statement"
) : CFGJumpNode(context, deepness, title)
{
    override fun linkable(to: CFGNode): Boolean = to is CFGNodeFunction
}

data class CFGNodeBreakStatement(
    override val context: Int,
    override val deepness: Int,
    override val title: String = "break statement"
) : CFGJumpNode(context, deepness, title, StyleCatalogue.NodeStyles.breaks)
{
    /** Флаг встречи с внешним итератором. */
    private var iteratorHappened = false

    override fun linkable(to: CFGNode): Boolean = when {
        this.deepness == to.deepness -> false
        iteratorHappened -> true
        to is CFGNodeFunction -> true
        to is CFGIterationNode -> { iteratorHappened = true; false }
        else -> false
    }
}

data class CFGNodeContinueStatement(
    override val context: Int,
    override val deepness: Int,
    override val title: String = "continue statement"
) : CFGJumpNode(context, deepness, title, StyleCatalogue.NodeStyles.breaks)

data class CFGNodeFunctionCall(
    override val context: Int,
    override val deepness: Int,
    override val title: String = "function call"
) : CFGNonBodyNode(context, deepness, title)
{
    override fun link(other: CFGNode, defStyle: LinkStyle?, vararg type: CFGLink.LinkType) {
        super.link(other, defStyle, CFGLink.LinkType.NONLINEAR)
    }
}
