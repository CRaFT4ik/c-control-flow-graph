package ru.er_log.cfg

import ru.er_log.antlr.RuleContextWithAltNumAutoInc.Companion.INIT_ALT_NUMBER


data class CFGraph(
        val graph: MutableList<CFGNode> = mutableListOf()
) {
    init {
        val context = INIT_ALT_NUMBER - 1
        add(CFGNodeFunction(context, "entry"))
        add(CFGNodeFunction(context, "exit"))
    }

    fun add(node: CFGNode) {
        if (graph.isNotEmpty()) { graph.last().link(node) }
        graph.add(node)
    }
}