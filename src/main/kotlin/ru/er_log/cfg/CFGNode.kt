package ru.er_log.cfg

import java.util.*

sealed class CFGNode(
        /** Контекст (он же UUID), в котором создавался узел.
          * Используется при завершении парсинга узла. */
        val context: Int,

        /** Название узла. */
        val title: String,

        /** Список ссылок (переходов) данного узла. */
        val links: MutableList<CFGNode> = mutableListOf(),

        /** Список узлов, которые ссылаются на данный узел. */
        val linkedStack: Stack<CFGNode> = Stack()
) {
    /** Крайний узел, к которому был привязан данный. */
    val lastLinked: CFGNode?
        get() = if (linkedStack.isNotEmpty()) { linkedStack.lastElement() } else { null }

    fun link(other: CFGNode) {
        links.add(other)
        other.linkedStack.add(this)
    }

    fun unlink(other: CFGNode) {
        links.remove(other)
        other.linkedStack.remove(this)
    }

    override fun toString(): String =
            String.format("%5d '$title'", context)

    override fun hashCode(): Int = context.hashCode()
    override fun equals(other: Any?): Boolean =
            other is CFGNode && other.hashCode() == this.hashCode()
}

class CFGNodeFunction       (context: Int, title: String = "function") : CFGNode(context, title)
class CFGNodeFunctionCall   (context: Int, title: String = "function call") : CFGNode(context, title)
class CFGNodeIfStatement    (context: Int, title: String = "if statement") : CFGNode(context, title)
class CFGNodeElseStatement  (context: Int, title: String = "else statement") : CFGNode(context, title)
