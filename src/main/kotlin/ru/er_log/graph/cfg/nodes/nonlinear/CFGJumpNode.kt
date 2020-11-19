package ru.er_log.graph.cfg.nodes.nonlinear

import ru.er_log.graph.LinkStyle
import ru.er_log.graph.NodeStyle
import ru.er_log.graph.StyleCatalogue
import ru.er_log.graph.cfg.nodes.CFGLink
import ru.er_log.graph.cfg.nodes.CFGNode
import ru.er_log.graph.cfg.nodes.CFGNonBodyNode
import ru.er_log.graph.cfg.nodes.linear.CFGIterationNode
import ru.er_log.graph.cfg.nodes.linear.CFGNodeFunction
import ru.er_log.graph.cfg.nodes.linear.CFGNodeLabel
import java.util.*

abstract class CFGJumpNode(
    context: Int,
    deepness: Int,
    title: String = "jump statement",
    style: NodeStyle = StyleCatalogue.NodeStyles.jump
) : CFGNonBodyNode(context, deepness, title, style)
{
    override fun isLinkable(to: CFGNode): Boolean = when(to) {
        is CFGNodeFunction -> true
        is CFGIterationNode -> true
        else -> false
    }

    /** Передаем все узлы, привязанные к данному, узлу наследнику. */
    override fun link(other: CFGNode, defStyle: LinkStyle?, vararg type: CFGLink.LinkType) {
        if (!isLinkable(other)) { return }

        while (this.linked.size > 0) {
            val link = this.linked.first()
            link.to.unlink(this)
            link.to.link(other, link.style, CFGLink.LinkType.NONLINEAR, *type)
        }
    }
}

data class CFGNodeGotoStatement(
    override val context: Int,
    override val deepness: Int,
    override val title: String = "goto statement"
) : CFGJumpNode(context, deepness, title)
{
    private val allowLinkingStack = Stack<CFGNode>()

    override fun isLinkable(to: CFGNode): Boolean = (to is CFGNodeLabel && to.title == title) || allowLinkingStack.contains(to)

    override fun link(other: CFGNode, defStyle: LinkStyle?, vararg type: CFGLink.LinkType) {
        if (isLinkable(other)) {
            val deputy = (other as CFGNodeLabel).getDeputy() ?: return
            // Разрешаем связывание с узлом-делегатом только на текущий момент.
            allowLinkingStack.push(deputy)
            super.link(deputy, defStyle, CFGLink.LinkType.NONLINEAR, CFGLink.LinkType.DIR_JUMP)
            allowLinkingStack.pop()
        }
    }
}

data class CFGNodeReturnStatement(
    override val context: Int,
    override val deepness: Int,
    override val title: String = "return statement"
) : CFGJumpNode(context, deepness, title)
{
    override fun isLinkable(to: CFGNode): Boolean = to is CFGNodeFunction
}

data class CFGNodeBreakStatement(
    override val context: Int,
    override val deepness: Int,
    override val title: String = "break statement"
) : CFGJumpNode(context, deepness, title, StyleCatalogue.NodeStyles.breaks)
{
    /** Флаг встречи с внешним итератором. */
    private var iteratorHappened = false

    override fun isLinkable(to: CFGNode): Boolean = when {
        iteratorHappened -> true
        to is CFGNodeFunction -> true
        to is CFGIterationNode -> findOldestCycle(this) === to
        else -> false
    }

    override fun link(other: CFGNode, defStyle: LinkStyle?, vararg type: CFGLink.LinkType) {
        if (!isLinkable(other)) { return }

        // Связываем только за следующим узлом после цикла.
        if (!iteratorHappened) { iteratorHappened = true }
        else { super.link(other, defStyle, *type) }
    }

    /**
     * Определяет цикл-предок для [current], находящийся на наименьшей глубине.
     *
     * @param current узел, для которого осуществляется поиск по предкам
     * @return  самый старший предок типа [CFGIterationNode] для узла [current],
     *          или null, если такого предка не обнаружено
     */
    private fun findOldestCycle(current: CFGNode, visited: MutableList<CFGNode> = mutableListOf()): CFGIterationNode? {
        if (visited.contains(current)) return null
        else visited.add(current)

        var oldestCycle: CFGIterationNode? = if (current is CFGIterationNode) current else null
        fun tryToSetOldest(client: CFGIterationNode?) {
            if (client == null) return
            if (oldestCycle == null || oldestCycle!!.deepness > client.deepness) oldestCycle = client
        }

        val neighbors = current.linked.map { it.to }.filter { it.deepness <= current.deepness }
        neighbors.forEach { node ->
            if (node is CFGIterationNode) tryToSetOldest(node)
            tryToSetOldest(findOldestCycle(node, visited.toMutableList()))
        }

        return oldestCycle
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
