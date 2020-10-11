package ru.er_log.graph.cfg.nodes.linear

import ru.er_log.graph.StyleCatalogue
import ru.er_log.graph.cfg.nodes.CFGBodyNode
import ru.er_log.graph.cfg.nodes.CFGLink

abstract class CFGIterationNode(context: Int, deepness: Int, title: String = "iteration statement") :
    CFGBodyNode(context, deepness, title, StyleCatalogue.NodeStyles.iteration)
{
    override fun onClose() {
        leaves().forEach { it.link(body.first(), CFGLink.LinkType.DIR_BACK) }
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
        leaves().forEach { it.link(this) }
        body.add(this)
        this.link(body.first(), CFGLink.LinkType.DIR_BACK)
    }
}