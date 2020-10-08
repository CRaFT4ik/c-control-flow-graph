package ru.er_log.graph.cfg

import com.github.aakira.napier.Napier
import java.util.*


data class CFGraph(
        /** Формируемый список связанных узлов.
          * Состоит из элементов, завершивших участие в парсинге. */
        val graph: MutableList<CFGNode> = mutableListOf(),

        /** Стек незакрытых блоков, ожидающих добавления в граф. */
        private val unclosedStack: Stack<CFGNode> = Stack()
) {
    fun start() {}
    fun finish() {} // graph.removeIf { it.links.isEmpty() && it.lastLinked == null }

    /**
     * Добавляет [node] в стек [unclosedStack] незакрытых элементов если он
     * является составным, или если [unclosedStack] содержит составные элементы.
     * Элемент будет добавлен в [graph] после закрытия методом [close].
     */
    fun enter(node: CFGNode) {
        log(" enter in $node")

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
    }

    /**
     * Вызывается, когда парсинг элемента завершается.
     * Находит элемент с заданным [context]. Если элемент составной, закрывает как его самого,
     * так и все дочерние (несмотря на то что они, возможно, не закрыты).
     */
    fun close(context: Int) {
        val node: CFGNode = findNode(context)
        log("exit from $node")

        if (node is CFGNodeFunction || node !is CFGLinearNode) { return }

        onCloseLinearBlock(node, unclosedStack.lastOrNull())

        val index = unclosedStack.indexOf(node)
        while (unclosedStack.size > index) {
            val pop = unclosedStack.pop()
            graph.add(pop)
        }
    }

    private fun onCloseLinearBlock(node: CFGNode, lastChild: CFGNode?) = when(node) {
        is CFGIterationNode -> link(lastChild, node, CFGLink.LinkType.BACKWARD)
        else -> {}
    }

    private fun link(from: CFGNode?, to: CFGNode?, linkType: CFGLink.LinkType? = null) {
        if (from == null || to == null) { return }

        val types: MutableList<CFGLink.LinkType> = mutableListOf()

        if (linkType != null) { types.add(linkType) }
        if (from is CFGNonLinearNode) { types.add(CFGLink.LinkType.NONLINEAR) }

        from.link(to, *types.toTypedArray())
    }

    private fun findNode(context: Int): CFGNode = when {
        unclosedStack.isNotEmpty() -> unclosedStack.find { it.context == context }!!
        else -> graph.find { it.context == context }!!
    }

    private fun log(message: String) {
        Napier.v(message)
    }
}