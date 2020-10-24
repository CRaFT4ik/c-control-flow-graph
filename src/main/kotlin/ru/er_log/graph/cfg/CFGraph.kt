package ru.er_log.graph.cfg

import com.github.aakira.napier.Napier
import ru.er_log.graph.cfg.nodes.CFGBodyNode
import ru.er_log.graph.cfg.nodes.CFGNode
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
//        // Удалим весь "мертвый код".
//        val deadNodes = graph.filter { it.linked.isEmpty() && it.deepness > 0 }
//
//        fun collectDead(parent: CFGNode, list: MutableList<CFGNode> = mutableListOf()): MutableList<CFGNode> {
//            list.add(parent)
//            parent.links.forEach { link -> list.addAll(collectDead(link.to, list)) }
//            return list
//        }
//
//        deadNodes.forEach { graph.removeAll(collectDead(it)) }
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
            Napier.v("  pushed ${node.title} into ${it.title}")
            it.push(node)
        }
        graph.add(node)
    }
}