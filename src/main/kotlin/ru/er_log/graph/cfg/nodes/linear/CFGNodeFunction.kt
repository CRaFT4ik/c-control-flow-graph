package ru.er_log.graph.cfg.nodes.linear

import ru.er_log.graph.StyleCatalogue
import ru.er_log.graph.cfg.nodes.CFGBodyNode

data class CFGNodeFunction(
    override val context: Int,
    override val deepness: Int,
    override val title: String = "function"
) : CFGBodyNode(context, deepness, title, StyleCatalogue.NodeStyles.function)