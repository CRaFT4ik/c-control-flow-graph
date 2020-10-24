package ru.er_log.graph.cfg.nodes.linear

import ru.er_log.graph.NodeStyle
import ru.er_log.graph.StyleCatalogue
import ru.er_log.graph.cfg.nodes.CFGBodyNode

data class CFGNodeFunction(
    override val context: Int,
    override val deepness: Int,
    override val title: String = "function",
    override val style: NodeStyle = StyleCatalogue.NodeStyles.function
) : CFGBodyNode(context, deepness, title, style)
{
    override fun onClose() {
        val exitNode = this.copy(title = "$title :: end", style = StyleCatalogue.NodeStyles.functionEnd)
        nodesForLinking().forEach { it.link(exitNode) }
        body.add(exitNode)
    }
}