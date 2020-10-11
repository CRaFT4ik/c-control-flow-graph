package ru.er_log.graph.cfg

import ru.er_log.graph.cfg.nodes.CFGBodyNode
import ru.er_log.graph.cfg.nodes.CFGLink
import ru.er_log.graph.cfg.nodes.CFGNode
import ru.er_log.graph.cfg.nodes.linear.CFGChoiceNode
import ru.er_log.graph.cfg.nodes.nonlinear.CFGNodeFunctionCall
import ru.er_log.graph.cfg.nodes.nonlinear.CFGNodeGotoStatement
import ru.er_log.graph.cfg.nodes.nonlinear.CFGNodeReturnStatement
import java.util.*

data class CFGraph(
    /** Формируемый список связанных узлов.
     * Состоит из элементов, завершивших участие в парсинге. */
    val graph: MutableList<CFGNode> = mutableListOf(),

    /** Стек незакрытых блоков, ожидающих добавления в граф. */
    private val bodyStack: Stack<CFGBodyNode> = Stack()
) {
    fun start() {}
    fun finish() {}

    /**
     * Вызывается, когда парсинг элемента только начинается.
     */
    fun enter(node: CFGNode) {
        //Napier.v(" enter in $node")

        if (node is CFGBodyNode) {
            node.onEnter()
            bodyStack.push(node)
        }
    }

    /**
     * Вызывается, когда парсинг элемента завершается.
     */
    fun close(node: CFGNode) {
        //Napier.v("exit from $node")

        if (node is CFGBodyNode) {
            node.onClose()
            bodyStack.pop()
        }

        bodyStack.lastOrNull()?.push(node)
        graph.add(node)
    }

    private fun link(from: CFGNode?, to: CFGNode?, linkType: CFGLink.LinkType? = null) {
        if (from == null || to == null) { return }

        val types: MutableList<CFGLink.LinkType> = mutableListOf()

        if (linkType != null) { types.add(linkType) }
        if (from is CFGNodeFunctionCall) { types.add(CFGLink.LinkType.NONLINEAR) }
        if (from is CFGChoiceNode) { types.add(CFGLink.LinkType.DIR_ALTER) }

        if (from is CFGNodeGotoStatement) { types.add(CFGLink.LinkType.DIR_JUMP) }
        if (from is CFGNodeReturnStatement) { types.add(CFGLink.LinkType.DIR_JUMP) }

        from.link(to, *types.toTypedArray())
    }
}