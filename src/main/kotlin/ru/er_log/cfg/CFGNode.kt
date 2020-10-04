package ru.er_log.cfg

sealed class CFGNode(
        val context: Int,
        val title: String,
        val links: MutableList<CFGNode> = mutableListOf()
) {
    fun link(other: CFGNode) {
        links.add(other)
    }
}

class CFGNodeFunction(context: Int, title: String = "function") : CFGNode(context, title)