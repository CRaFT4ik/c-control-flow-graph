package ru.er_log.graph.cfg.nodes.linear

import ru.er_log.graph.NodeStyle
import ru.er_log.graph.StyleCatalogue
import ru.er_log.graph.cfg.nodes.CFGBodyNode
import ru.er_log.graph.cfg.nodes.nonlinear.CFGNodeGotoStatement

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
        linkGotoWithLabels()
        body.add(exitNode)
    }

    /** Отдельно, также необходимо соединить все метки,
      * поскольку они могут не являться листьями. */
    private fun linkGotoWithLabels() {
        val gotos = collectAllNodes<CFGNodeGotoStatement>()
        val labels = collectAllNodes<CFGNodeLabel>()

        // Проверки на возможность соединения осуществляются уровнем дальше.
        labels.forEach { label -> gotos.forEach { goto -> goto.link(label) } }
    }
}