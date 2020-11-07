package ru.er_log.graph.cfg.nodes.linear

import ru.er_log.graph.cfg.nodes.CFGBodyNode
import ru.er_log.graph.cfg.nodes.CFGLink
import ru.er_log.graph.cfg.nodes.CFGNode

data class CFGNodeLabel(
        override val context: Int,
        override val deepness: Int,
        override val title: String = "label"
) : CFGBodyNode(context, deepness, title)
{
    override fun onEnter() {}

    /**
     * Определяет узел-заместитель, к которому будут привязываться узлы вместо данного.
     */
    fun getDeputy(): CFGNode? = body.firstOrNull().let { if (it is CFGNodeLabel) it.getDeputy() else it }

    override fun push(other: CFGNode, linkType: CFGLink.LinkType) {
        if (body.firstOrNull() == null && other is CFGNodeLabel)
            throw IllegalStateException("Found 2 or more labels following each other. It's not supported yet!")

        super.push(other, linkType)
    }
}