package ru.er_log.graph.cfg

import com.github.aakira.napier.Napier
import java.util.*


data class CFGraph(
        /** Формируемый список связанных узлов. */
        val graph: MutableList<CFGNode> = mutableListOf(),

        /** Стек незакрытых блоков, ожидающих добавления в граф. */
        private val unclosedStack: Stack<CFGNode> = Stack()
) {
    private val lastNode: CFGNode
        get() = graph[graph.lastIndex]

    fun start() {

    }

    fun finish() {
//        graph.removeIf { it is CFGNodeFunction && it.title != "main()" }
        graph.removeIf { it.links.isEmpty() && it.lastLinked == null }
    }

    /**
     * Добавляет [node] в стек [unclosedStack] незакрытых элементов.
     * Элемент будет добавлен в [graph] после закрытия методом [close].
     */
    fun enter(node: CFGNode) {
        log(" enter in $node")

//        graph.lastOrNull()?.takeIf { it !is CFGNodeFunction }?.link(node)

//        when(node) {
//            is CFGLinearNode -> unclosedStack.add(node)
//            is CFGNonLinearNode -> graph.add(node)
//        }

        when {
            node is CFGNodeFunction -> {}
            unclosedStack.isNotEmpty() -> link(unclosedStack.last(), node)
            else -> link(graph.lastOrNull(), node)
        }

        when (node) {
            is CFGNodeFunction -> graph.add(node)
            is CFGLinearNode -> unclosedStack.add(node)
            else -> when {
                unclosedStack.isNotEmpty() -> unclosedStack.add(node)
                else -> graph.add(node)
            }
        }

//        if (node is CFGLinearNode) {
//            clone(node) tie node
//            when (node) {
////            is CFGNodeFunction -> enterFunction(node)
////            is CFGNodeFunctionCall -> enterFunctionCall(node)
////            is CFGNodeIfStatement -> enterIfStatement(node)
////            is CFGNodeElseStatement -> enterElseStatement(node)
//                is CFGIterationNode -> enterIterStatement(node)
//            }
//        }

        //unclosedStack.lastOrNull()?.link(closedNode)

//            if (node is CFGNodeIterStatement) {
//                closedNode.link(node)
//            }

//        if (unclosedStack.isNotEmpty()) {
//            unclosedStack.last().link(node)
//        } else {
//            graph.lastOrNull()?.link(node)
//        }

//        if (node !is CFGNodeFunction)
//            graph.lastOrNull()?.link(node)
//        graph.add(node)
    }

    /**
     * Находит элемент с заданным [context] в [unclosedStack] и закрывает как сам
     * элемент, так и все его дочерние (несмотря на то что они, возможно, не закрыты).
     */
    fun close(context: Int) {
        val node: CFGNode = findNode(context)

        if (node is CFGNodeFunction || node !is CFGLinearNode) { return }

        log("exit from $node")

        onCloseLinearBlock(node, unclosedStack.lastOrNull())

        val index = unclosedStack.indexOf(node)
        while (unclosedStack.size > index) {
            val pop = unclosedStack.pop()
            graph.add(pop)
        }
    }

    private fun onCloseLinearBlock(node: CFGNode, lastChild: CFGNode?) = when(node) {
        is CFGIterationNode -> link(lastChild, node, Link.LinkType.BACKWARD)
        else -> {}
    }

    private fun link(from: CFGNode?, to: CFGNode?, linkType: Link.LinkType? = null) {
        if (from == null || to == null) { return }

        val types: MutableList<Link.LinkType> = mutableListOf()

        if (linkType != null) { types.add(linkType) }
        if (from is CFGNonLinearNode) { types.add(Link.LinkType.NONLINEAR) }

        from.link(to, *types.toTypedArray())
    }

    private fun findAllDeepestLinear(nodes: List<CFGNode>): List<CFGLinearNode> {
        return nodes.filterIsInstance<CFGLinearNode>().map { it.adjacentNode }
    }

    private fun findNode(context: Int): CFGNode = when {
        unclosedStack.isNotEmpty() -> unclosedStack.find { it.context == context }!!
        else -> graph.find { it.context == context }!!
    }

    private fun log(message: String) {
        Napier.v(message)
    }

    private fun error(node: CFGNode) {
        throw IllegalStateException("Unexpected input: $node after $lastNode")
    }

//    private fun remove(node: CFGNode) {
//        graph.remove(node)
//        graph.forEach { it.unlink(node) }
//    }

    private fun enterFunction(node: CFGNodeFunction) {

    }

    private fun enterFunctionCall(node: CFGNodeFunctionCall) {

    }

    private fun enterIterStatement(node: CFGIterationNode) {

    }

    private fun closeIterStatement(node: CFGIterationNode) {

    }

    private fun enterIfStatement(node: CFGNodeIfStatement) {
//        if (lastNode is CFGNodeElseStatement) { remove(lastNode) } // Удаляем 'else', потому что видим 'else if'.
//        lastNode.link(node)
    }

    private fun enterElseStatement(node: CFGNodeElseStatement) {
//        if (lastNode !is CFGNodeIfStatement) { error(node) }
//        lastNode.lastLinked!!.link(node) // Привязываемся к родительскому узлу (по отношению к 'if').
    }
}

//private fun CFGGraph.getLastUnclosedIf() : CFGNodeIfStatement? {
//    graph.findLast { it is CFGNodeIfStatement }
//}