package ru.er_log.graph.cfg.nodes.linear

import ru.er_log.graph.cfg.nodes.CFGNonBodyNode

data class CFGNodeCodeBlock(
        override val context: Int,
        override val deepness: Int,
        override val title: String = "code block"
) : CFGNonBodyNode(context, deepness, title)