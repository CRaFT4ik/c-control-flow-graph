package ru.er_log.graph.cfg

import com.github.aakira.napier.Napier
import java.util.*


data class CFGraph(
        /** Формируемый список связанных узлов.
          * Состоит из элементов, завершивших участие в парсинге. */
        val graph: MutableList<CFGNode> = mutableListOf(),

        /** Стек незакрытых блоков, ожидающих добавления в граф. */
        private val unclosedStack: Stack<CFGNode> = Stack(),

        /** Список ожидающих линковки.
          * Ключ - узел, при вхождении в который нужно осуществить линковку;
          * Значение - стек ожидающий линковки. После линковки делается pop(). */
        private val linkWaiters: HashMap<CFGLinearNode, Stack<Pair<CFGNode, CFGLink.LinkType?>>> = hashMapOf()
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
            unclosedStack.isNotEmpty() -> if (!onEnterLinearBlock(unclosedStack.last(), node)) { return }
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
     * Возвращает false, если за блоком [lastParent] нельзя ничего добавлять.
     * @param lastParent CFGNode
     * @param node CFGNode
     * @return Boolean
     */
    private fun onEnterLinearBlock(lastParent: CFGNode, node: CFGNode) : Boolean {
        when (lastParent) {
            is CFGChoiceNode -> link(lastParent, node, CFGLink.LinkType.DIR_PRIM)
            is CFGJumpNode -> return false
            else -> link(lastParent, node)
        }
        return true
    }

    private fun findLastIteratorNode(): CFGIterationNode? {
        return unclosedStack.filterIsInstance<CFGIterationNode>().lastOrNull()
    }

    /**
     * Вызывается, когда парсинг элемента завершается.
     * Находит элемент с заданным [context]. Если элемент составной, закрывает как его самого,
     * так и все дочерние (несмотря на то что они, возможно, не закрыты).
     */
    fun close(context: Int) {
        val node = findNode(context) ?: return

        log("exit from $node")

        if (node is CFGNodeFunction || node !is CFGLinearNode) { return }

        onCloseLinearBlock(node, unclosedStack.lastOrNull())

        val index = unclosedStack.indexOf(node)
        while (index >= 0 && unclosedStack.size > index) {
            val pop = unclosedStack.pop()
            graph.add(pop)
        }
    }

    private fun onCloseLinearBlock(node: CFGLinearNode, lastChild: CFGNode?) = when(node) {
//        is CFGChoiceNode -> when(lastChild) {
//            is CFGNode -> waitChild(node, lastChild, CFGLink.LinkType.DEFAULT)
//            else -> {}
//        }
        is CFGIterationNode -> when(lastChild) {
            is CFGNodeBreakStatement -> waitChild(node, lastChild, CFGLink.LinkType.DIR_JUMP)
            is CFGNodeReturnStatement -> {}
            else -> link(lastChild, node, CFGLink.LinkType.DIR_BACK)
        }
        else -> {}
    }

    private fun link(from: CFGNode?, to: CFGNode?, linkType: CFGLink.LinkType? = null) {
        if (from == null || to == null) { return }

        val types: MutableList<CFGLink.LinkType> = mutableListOf()

        if (linkType != null) { types.add(linkType) }
        if (from is CFGNodeFunctionCall) { types.add(CFGLink.LinkType.NONLINEAR) }
        if (from is CFGChoiceNode) { types.add(CFGLink.LinkType.DIR_ALTER) }

        if (from is CFGNodeGotoStatement) { types.add(CFGLink.LinkType.DIR_JUMP) }
        if (from is CFGNodeReturnStatement) { types.add(CFGLink.LinkType.DIR_JUMP) }

        val waiter = linkWaiters[from]?.pop()
        link(waiter?.first, to, waiter?.second)

        from.link(to, *types.toTypedArray())
    }

    private fun waitChild(parent: CFGLinearNode, waiter: CFGNode, linkType: CFGLink.LinkType?) {
        val stack = linkWaiters[parent] ?: Stack()
        stack.push(waiter to linkType)
        linkWaiters[parent] = stack
    }

    private fun findNode(context: Int): CFGNode? {
        return unclosedStack.find { it.context == context } ?: graph.find { it.context == context }
    }

    private fun log(message: String) {
        Napier.v(message)
    }
}