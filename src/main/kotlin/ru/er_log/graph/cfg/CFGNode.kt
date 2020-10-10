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

    open fun link(other: CFGNode, vararg type: CFGLink.LinkType) {
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
abstract class CFGNonBodyNode(context: Int, title: String, style: NodeStyle = StyleCatalogue.NodeStyles.default) : CFGNode(context, title, style)

/**
 * Узел с линейным выполнением.
 */
abstract class CFGBodyNode(context: Int, title: String, style: NodeStyle = StyleCatalogue.NodeStyles.default): CFGNode(context, title, style)
{
    protected val body: Stack<CFGNode> = Stack()

    open fun onEnter() {
        body.add(this)
    }

    open fun onClose() {}

//    override fun link(other: CFGNode, vararg type: CFGLink.LinkType) {
//        when (val last = body.last()) {
//            this -> super.link(other, *type)
//            else -> last.link(other, *type)
//        }
//    }

    open fun push(other: CFGNode) {
        when (other) {
            is CFGBodyNode -> body.last().link(other.body.first())
            else -> body.last().link(other)
        }
        body.add(other)
    }
}

data class CFGNodeFunction          (override val context: Int, override val title: String = "function")            : CFGBodyNode(context, title, StyleCatalogue.NodeStyles.function)

abstract class CFGChoiceNode        (context: Int, title: String = "choice statement") : CFGBodyNode(context, title, StyleCatalogue.NodeStyles.choice)
data class CFGNodeIfStatement       (override val context: Int, override val title: String = "if statement")        : CFGChoiceNode(context, title)
data class CFGNodeElseIfStatement   (override val context: Int, override val title: String = "else if statement")   : CFGChoiceNode(context, title)
data class CFGNodeElseStatement     (override val context: Int, override val title: String = "else statement")      : CFGChoiceNode(context, title)

abstract class CFGIterationNode     (context: Int, title: String = "iteration statement") : CFGBodyNode(context, title, StyleCatalogue.NodeStyles.iteration)
data class CFGNodeForStatement      (override val context: Int, override val title: String = "for statement")       : CFGIterationNode(context, title)
data class CFGNodeWhileStatement    (override val context: Int, override val title: String = "while statement")     : CFGIterationNode(context, title)
{
    override fun onClose() {
        body.last().link(body.first(), CFGLink.LinkType.DIR_BACK)
    }
}
data class CFGNodeDoWhileStatement  (override val context: Int, override val title: String = "do while statement")  : CFGIterationNode(context, title)
{
    override fun onEnter() {}

    override fun onClose() {
        body.lastOrNull()?.link(this)
        body.add(this)
        link(body.first(), CFGLink.LinkType.DIR_BACK)
    }

    override fun push(other: CFGNode) {
        body.lastOrNull()?.link(other)
        body.add(other)
    }
}

abstract class CFGJumpNode          (context: Int, title: String = "jump statement", style: NodeStyle = StyleCatalogue.NodeStyles.jump) : CFGNonBodyNode(context, title, style)
data class CFGNodeGotoStatement     (override val context: Int, override val title: String = "goto statement")      : CFGJumpNode(context, title)
data class CFGNodeReturnStatement   (override val context: Int, override val title: String = "return statement")    : CFGJumpNode(context, title)
data class CFGNodeContinueStatement (override val context: Int, override val title: String = "continue statement")  : CFGJumpNode(context, title, StyleCatalogue.NodeStyles.breaks)
data class CFGNodeBreakStatement    (override val context: Int, override val title: String = "break statement")     : CFGJumpNode(context, title, StyleCatalogue.NodeStyles.breaks)

data class CFGNodeFunctionCall      (override val context: Int, override val title: String = "function call")       : CFGNonBodyNode(context, title)
