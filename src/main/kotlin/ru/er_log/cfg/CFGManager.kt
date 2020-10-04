package ru.er_log.cfg

import ru.er_log.antlr.ANTLRManager


class CFGManager(input: String)
{
    private val graph: CFGraph = CFGraph()
    private val listener = CFGListener(graph)
    private val antlrManager: ANTLRManager = ANTLRManager(input, listener)

    fun calculate(): CFGResult {
        antlrManager.run()
        return CFGResult(graph.copy())
    }
}

data class CFGResult(
        val graph: CFGraph
) {
    fun toImage(): ByteArray {
        return ByteArray(0)
    }
}