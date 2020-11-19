package ru.er_log.graph.cfg.nodes.linear

import ru.er_log.graph.LinkStyle
import ru.er_log.graph.NodeStyle
import ru.er_log.graph.StyleCatalogue
import ru.er_log.graph.cfg.nodes.CFGBodyNode
import ru.er_log.graph.cfg.nodes.CFGLink
import ru.er_log.graph.cfg.nodes.CFGNode
import java.util.*

abstract class CFGIterationNode(
    context: Int,
    deepness: Int,
    title: String = "iteration statement",
    style: NodeStyle = StyleCatalogue.NodeStyles.iteration
) : CFGBodyNode(context, deepness, title, style)
{
    override fun onClose() {
        nodesForLinking().forEach { it.link(body.first(), null, CFGLink.LinkType.DIR_BACK) }
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
    val nodeType: NodeType,
    override val context: Int,
    override val deepness: Int,
    private val _title: String = "for statement",
    override val style: NodeStyle = if (nodeType === NodeType.CONDITION) {
        StyleCatalogue.NodeStyles.choiceInCycle
    } else {
        StyleCatalogue.NodeStyles.default
    }
) : CFGIterationNode(context, deepness, _title, style)
{
    enum class NodeType { INITIAL, CONDITION, INCREMENT }

    override val title = if (nodeType === NodeType.CONDITION) "if ($_title)" else _title

    override fun onClose() {
        if (nodeType !== NodeType.CONDITION) return
        nodesForLinking().forEach { it.link(body.first(), null, CFGLink.LinkType.DIR_BACK) }
    }

    override fun link(other: CFGNode, defStyle: LinkStyle?, vararg type: CFGLink.LinkType) {
        val linkStyle = if (nodeType === NodeType.CONDITION && this.linked.isEmpty()) { StyleCatalogue.LinkStyles.succeed } else { defStyle }
        val linkType = if (nodeType === NodeType.INCREMENT && this.linked.isEmpty()) { CFGLink.LinkType.DIR_BACK } else  { CFGLink.LinkType.DEFAULT }
        super.link(other, linkStyle, *type, linkType)
    }

    override fun leaves(): MutableSet<CFGNode> {
        if (nodeType === NodeType.CONDITION) {
            val leaves = super.leaves()
            leaves.removeIf { it is CFGNodeForStatement && it.context == this.context && it.nodeType !== NodeType.CONDITION }
        }

        return super.leaves()
    }
}

data class CFGNodeWhileStatement(
    override val context: Int,
    override val deepness: Int,
    private val _title: String = "while statement",
    override val style: NodeStyle = StyleCatalogue.NodeStyles.choiceInCycle
) : CFGIterationNode(context, deepness, _title, style)
{
    override val title = "while ($_title)"
}

data class CFGNodeDoWhileStatement(
    override val context: Int,
    override val deepness: Int,
    private val _title: String = "do while statement",
    override val style: NodeStyle = StyleCatalogue.NodeStyles.choiceInCycle
) : CFGIterationNode(context, deepness, _title, style)
{
    override val title = "while ($_title)"

    override fun onEnter() {}

    override fun onClose() {
        nodesForLinking().forEach { it.link(this, null, CFGLink.LinkType.DIR_BACK) }
        body.add(this)
        this.link(body.first(), null, CFGLink.LinkType.DIR_PRIM)
    }
}