package ru.er_log.graph.cfg

import ru.er_log.graph.NodeStyle
import ru.er_log.graph.StyleCatalogue
import ru.er_log.graph.cfg.CFGLink.Companion.calculateLinkStyle
import java.util.*


sealed class CFGNode(
        /** Контекст (uuid) создания узла.
          * Можно опознать открывающий \ закрывающий узлы. */
        open val context: Int,

        /** Название узла. */
        open val title: String,

        /** Стиль отображения узла. */
        open val style: NodeStyle = StyleCatalogue.NodeStyles.default,

        /** Уникальный порядковый номер создания узла.
          * В отличии от [context], будет разным для всех объектов. */
        val uid: Int = getUID(),

        /** Список ссылок (переходов) данного узла. */
        val links: MutableList<CFGLink> = mutableListOf(),

        /** Список узлов, которые ссылаются на данный узел. */
        private val linked: Stack<CFGLink> = Stack()
) {
    /** Крайний узел, к которому был привязан данный. */
    val lastLinked: CFGNode?
        get() = linked.lastOrNull()?.to

    fun link(other: CFGNode, vararg type: CFGLink.LinkType) {
        val style = calculateLinkStyle(*type)
        links.add(CFGLink(other, style))
        other.linked.add(CFGLink(this, style))
    }

    companion object {
        private var counter = 0
        @Synchronized fun getUID(): Int = counter++
    }
}

/**
 * Узел с нелинейным выполнением.
 */
abstract class CFGNonLinearNode(context: Int, title: String, style: NodeStyle = StyleCatalogue.NodeStyles.default) : CFGNode(context, title, style)

/**
 * Узел с линейным выполнением.
 */
abstract class CFGLinearNode(context: Int, title: String, style: NodeStyle = StyleCatalogue.NodeStyles.default): CFGNode(context, title, style)
{
    /** Ссылка на копию данного узла.
      * Узел состоит из двух частей: открывающего и закрывающего блоков. */
    lateinit var adjacentNode: CFGLinearNode

    companion object {
        infix fun CFGLinearNode.tie(other: CFGLinearNode) {
            this.adjacentNode = other
            other.adjacentNode = this
        }
    }
}

data class CFGNodeFunction          (override val context: Int, override val title: String = "function")            : CFGNode(context, title, StyleCatalogue.NodeStyles.function)

abstract class CFGChoiceNode        (context: Int, title: String = "choice statement") : CFGLinearNode(context, title, StyleCatalogue.NodeStyles.choice)
data class CFGNodeIfStatement       (override val context: Int, override val title: String = "if statement")        : CFGChoiceNode(context, title)
data class CFGNodeElseIfStatement   (override val context: Int, override val title: String = "else if statement")   : CFGChoiceNode(context, title)
data class CFGNodeElseStatement     (override val context: Int, override val title: String = "else statement")      : CFGChoiceNode(context, title)

abstract class CFGIterationNode     (context: Int, title: String = "iteration statement") : CFGLinearNode(context, title, StyleCatalogue.NodeStyles.iteration)
data class CFGNodeForStatement      (override val context: Int, override val title: String = "for statement")       : CFGIterationNode(context, title)
data class CFGNodeWhileStatement    (override val context: Int, override val title: String = "while statement")     : CFGIterationNode(context, title)
data class CFGNodeDoWhileStatement  (override val context: Int, override val title: String = "do while statement")  : CFGIterationNode(context, title)

abstract class CFGJumpNode          (context: Int, title: String = "jump statement", style: NodeStyle = StyleCatalogue.NodeStyles.jump) : CFGNonLinearNode(context, title, style)
data class CFGNodeGotoStatement     (override val context: Int, override val title: String = "goto statement")      : CFGJumpNode(context, title)
data class CFGNodeReturnStatement   (override val context: Int, override val title: String = "return statement")    : CFGJumpNode(context, title)
data class CFGNodeContinueStatement (override val context: Int, override val title: String = "continue statement")  : CFGJumpNode(context, title, StyleCatalogue.NodeStyles.breaks)
data class CFGNodeBreakStatement    (override val context: Int, override val title: String = "break statement")     : CFGJumpNode(context, title, StyleCatalogue.NodeStyles.breaks)

data class CFGNodeFunctionCall      (override val context: Int, override val title: String = "function call")       : CFGNonLinearNode(context, title)
