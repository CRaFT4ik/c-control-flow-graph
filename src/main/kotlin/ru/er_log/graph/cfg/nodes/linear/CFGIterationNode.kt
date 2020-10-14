package ru.er_log.graph.cfg.nodes.linear

import ru.er_log.graph.StyleCatalogue
import ru.er_log.graph.cfg.nodes.CFGBodyNode
import ru.er_log.graph.cfg.nodes.CFGLink
import ru.er_log.graph.cfg.nodes.CFGNode
import java.util.*

abstract class CFGIterationNode(context: Int, deepness: Int, title: String = "iteration statement") :
    CFGBodyNode(context, deepness, title, StyleCatalogue.NodeStyles.iteration)
{
    override fun onClose() {
        nodesForLinking().forEach { it.link(body.first(), CFGLink.LinkType.DIR_BACK) }
    }

    /** Смотреть описание [leaves]. */
    private var wasLinked: Boolean = false

    /**
     * Поскольку циклы имеют, как правило, цикловидную структуру графа, для того чтобы
     * соединить цикл со следующим элементом внутри одного блока, необходимо вручную
     * отслеживать, является ли цикл уже соединенным с чем-либо еще, помимо своего тела.
     */
    override fun leaves(): MutableSet<CFGNode> {
        val leaves = super.leaves()
        if (!wasLinked && leaves.isEmpty() && haveLoop()) {
            leaves.add(this)
            wasLinked = true
        }
        return leaves
    }

    /**
     * Рекурсивный поиск данного узла во всех подузлах [CFGNode.links].
     *
     * @param entry     вершина начала поиска
     * @param visited   список посещенных вершин
     * @return true, если совпадение найдено
     */
    private fun haveLoop(entry: CFGNode = this, visited: List<CFGNode> = mutableListOf(entry)): Boolean {
        entry.links.forEach { link ->
            if (link.to == this) { return true }

            val nodeVisited = visited.toMutableList()
            if (nodeVisited.contains(link.to)) { return@forEach }
            else { nodeVisited.add(link.to) }

            if (haveLoop(link.to, nodeVisited)) { return true }
        }
        return false
    }
}

data class CFGNodeForStatement(
    override val context: Int,
    override val deepness: Int,
    override val title: String = "for statement"
) : CFGIterationNode(context, deepness, title)

data class CFGNodeWhileStatement(
    override val context: Int,
    override val deepness: Int,
    override val title: String = "while statement"
) : CFGIterationNode(context, deepness, title)

data class CFGNodeDoWhileStatement(
    override val context: Int,
    override val deepness: Int,
    override val title: String = "do while statement"
) : CFGIterationNode(context, deepness, title)
{
    override fun onEnter() {}

    override fun onClose() {
        nodesForLinking().forEach { it.link(this) }
        body.add(this)
        this.link(body.first(), CFGLink.LinkType.DIR_BACK)
    }
}