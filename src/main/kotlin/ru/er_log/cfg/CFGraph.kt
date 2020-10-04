package ru.er_log.cfg

import com.github.aakira.napier.Napier
import ru.er_log.antlr.RuleContextWithAltNumAutoInc.Companion.INIT_ALT_NUMBER
import java.util.*


data class CFGraph(
        /** Формируемый список связанных узлов. */
        val graph: MutableList<CFGNode> = mutableListOf(),

        /** Стек текущего обрабатываемого блока. */
        private val stack: Stack<CFGNode> = Stack()
) {
    private val entryNode = CFGNodeFunction(INIT_ALT_NUMBER - 1, "entry")
//    private val exitNode  = CFGNodeFunction(INIT_ALT_NUMBER - 1, "exit")
//
    init {
        enter(entryNode)
//        entryNode.link(exitNode)
    }
//
    private val lastNode: CFGNode
        get() = graph[graph.lastIndex - 1]

    fun enter(node: CFGNode) {
        log(" enter in $node")

        when(node) {
            is CFGNodeFunction -> enterFunction(node)
            is CFGNodeFunctionCall -> enterFunctionCall(node)
            is CFGNodeIfStatement -> enterIfStatement(node)
            is CFGNodeElseStatement -> enterElseStatement(node)
        }

        graph.add(node)
    }

    fun close(node: CFGNode) {
        log("exit from $node")
    }

    private fun log(message: String) {
        Napier.v(message)
    }

    private fun error(node: CFGNode) {
        throw IllegalStateException("Unexpected input: $node after $lastNode")
    }

    private fun remove(node: CFGNode) {
        graph.remove(node)
        graph.forEach { it.unlink(node) }
    }

    private fun enterFunction(node: CFGNodeFunction) {}

    private fun enterFunctionCall(node: CFGNodeFunctionCall) {

    }

    private fun enterIfStatement(node: CFGNodeIfStatement) {
        if (lastNode is CFGNodeElseStatement) { remove(lastNode) } // Удаляем 'else', потому что видим 'else if'.
        lastNode.link(node)
    }

    private fun enterElseStatement(node: CFGNodeElseStatement) {
        if (lastNode !is CFGNodeIfStatement) { error(node) }
        lastNode.lastLinked!!.link(node) // Привязываемся к родительскому узлу (по отношению к 'if').
    }
}