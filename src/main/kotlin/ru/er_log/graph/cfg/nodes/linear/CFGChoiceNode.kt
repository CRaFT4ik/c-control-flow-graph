package ru.er_log.graph.cfg.nodes.linear

import ru.er_log.graph.StyleCatalogue
import ru.er_log.graph.cfg.nodes.CFGBodyNode
import ru.er_log.graph.cfg.nodes.CFGLink
import ru.er_log.graph.cfg.nodes.CFGNode

abstract class CFGChoiceNode(context: Int, deepness: Int, title: String = "choice statement") :
    CFGBodyNode(context, deepness, title, StyleCatalogue.NodeStyles.choice)
{
    private var closed = false

    override fun onClose() {
        closed = true
        super.onClose()
    }

    override fun leaves(): MutableSet<CFGNode> {
        val leaves = super.leaves()
        if (closed && this.links.size < 2) { leaves.add(this) }
        return leaves
    }

    override fun push(other: CFGNode, linkType: CFGLink.LinkType) {
        when (other) {
            is CFGNodeElseIfStatement -> { this.link(other); body.add(other) }
            is CFGNodeElseStatement -> { other.body.firstOrNull()?.let { this.link(it); body.addAll(other.body) } }
            else -> when {
                body.lastOrNull() == this -> super.push(other, CFGLink.LinkType.DIR_PRIM)
                else -> super.push(other, linkType)
            }
        }
    }
}

data class CFGNodeIfStatement(
    override val context: Int,
    override val deepness: Int,
    private val _title: String = "if statement"
) : CFGChoiceNode(context, deepness, _title)
{
    override val title = "if ($_title)"
}

data class CFGNodeElseIfStatement(
    override val context: Int,
    override val deepness: Int,
    private val _title: String = "else if statement"
) : CFGChoiceNode(context, deepness, _title)
{
    override val title = "else if ($_title)"
}

data class CFGNodeElseStatement(
    override val context: Int,
    override val deepness: Int,
    private val _title: String = "else statement"
) : CFGChoiceNode(context, deepness, _title)
{
    override fun onEnter() {}
}