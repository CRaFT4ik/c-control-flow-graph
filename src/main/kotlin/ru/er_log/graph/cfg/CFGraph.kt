package ru.er_log.graph.cfg

import com.github.aakira.napier.Napier
import ru.er_log.graph.cfg.nodes.CFGBodyNode
import ru.er_log.graph.cfg.nodes.CFGNode
import ru.er_log.graph.cfg.nodes.linear.CFGNodeFunction
import java.util.*

data class CFGraph(
    /** Формируемый список связанных узлов.
     * Состоит из элементов, завершивших участие в парсинге. */
    val graph: MutableList<CFGNode> = mutableListOf(),

    /** Стек незакрытых блоков, ожидающих добавления в граф. */
    private val bodyStack: Stack<CFGBodyNode> = Stack()
) {
    fun start() {}

    fun finish() {
        /* Пометим весь мертвый код. */
        graph.forEach { node -> if (findParentFunction(node) == null) { node.isDeadCode = true } }
    }

    /**
     * Вызывается, когда парсинг элемента только начинается.
     */
    fun enter(node: CFGNode) {
        Napier.i("enter in ${node.title}")

        if (node is CFGBodyNode) {
            node.onEnter()
            bodyStack.push(node)
        }
    }

    /**
     * Вызывается, когда парсинг элемента завершается.
     */
    fun close(node: CFGNode) {
        Napier.w("exit from ${node.title}")

        if (node is CFGBodyNode) {
            bodyStack.pop().onClose()
        }

        bodyStack.lastOrNull()?.let {
            Napier.v("  pushing ${node.title} into ${it.title}")
            it.push(node)
        }
        graph.add(node)
    }

    /**
     * Ищет функцию-родитель для узла [node].
     *
     * @param node узел, для которого осуществляется поиск
     * @return узел [CFGNodeFunction], который является родительским для узла [node]
     */
    private fun findParentFunction(node: CFGNode, visited: MutableList<CFGNode> = mutableListOf()): CFGNodeFunction? {
        if (visited.contains(node)) return null
        else visited.add(node)

        var result: CFGNodeFunction? = if (node is CFGNodeFunction) node else null
        fun tryToSetResult(client: CFGNodeFunction?) {
            if (client == null) return
            if (result == null || result!!.deepness > client.deepness) result = client
        }

        val neighbors = node.linked.map { it.to }

        neighbors.forEach { neighbor ->
            if (neighbor is CFGNodeFunction) tryToSetResult(neighbor)
            tryToSetResult(findParentFunction(neighbor, visited.toMutableList()))
        }

        return result
    }
}