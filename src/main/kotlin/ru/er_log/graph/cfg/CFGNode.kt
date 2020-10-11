package ru.er_log.graph.cfg

import com.github.aakira.napier.Napier
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
    val body: Stack<CFGNode> = Stack()

    open fun onEnter() {
        Napier.i("enter in ${this.title}")
        body.add(this)
    }

    open fun onClose() {
        Napier.w("exit from ${this.title}")
    }

    open fun push(other: CFGNode, linkType: CFGLink.LinkType = CFGLink.LinkType.DEFAULT) {
        Napier.v("  pushed ${other.title} into ${this.title}")

        val linkTo = when (other) {
            is CFGBodyNode -> other.body.first()
            else -> other
        }

        leaves().forEach { it.link(linkTo, linkType) }
        body.add(other)
    }

    open fun leaves(): Set<CFGNode> {
        val leaves = formLeaves()
        if (leaves.isEmpty()) { body.lastOrNull()?.let { leaves.add(it) } }
        return leaves
    }

    /** Собирает все листья текущего подграфа. */
    open fun formLeaves(): MutableSet<CFGNode> {
        val list = mutableSetOf<CFGNode>()
        body.forEach { node ->
            if (node.uid == 9)
                println(1)
            when {
                node.links.none { it.to != node } -> list.add(node)
                node is CFGBodyNode && node != this -> list.addAll(node.formLeaves())
            }
        }
        return list
    }
}

data class CFGNodeFunction          (override val context: Int, override val title: String = "function")            : CFGBodyNode(context, title, StyleCatalogue.NodeStyles.function)

abstract class CFGChoiceNode        (context: Int, title: String = "choice statement") : CFGBodyNode(context, title, StyleCatalogue.NodeStyles.choice)
{
    private var closed = false

    override fun onClose() {
        closed = true
        super.onClose()
    }

    override fun formLeaves(): MutableSet<CFGNode> {
        val leaves = super.formLeaves()
        if (closed && this.links.size < 2) { leaves.add(this) }
        return leaves
    }

    override fun push(other: CFGNode, linkType: CFGLink.LinkType) {
        when (other) {
            is CFGNodeElseIfStatement -> { this.link(other); body.add(other) }
            is CFGNodeElseStatement -> { other.body.firstOrNull()?.let { this.link(it); body.addAll(other.body) } }
            else -> when {
                body.lastOrNull() == this -> super.push(other, CFGLink.LinkType.DIR_PRIM)
                else -> super.push(other, linkType)
            }
        }
    }
}
data class CFGNodeIfStatement       (override val context: Int, override val title: String = "if statement")        : CFGChoiceNode(context, title)
data class CFGNodeElseIfStatement   (override val context: Int, override val title: String = "else if statement")   : CFGChoiceNode(context, title)
data class CFGNodeElseStatement     (override val context: Int, override val title: String = "else statement")      : CFGChoiceNode(context, title)
{
    override fun onEnter() {}
}
abstract class CFGIterationNode     (context: Int, title: String = "iteration statement") : CFGBodyNode(context, title, StyleCatalogue.NodeStyles.iteration)
{
    override fun onClose() {
        leaves().forEach { it.link(body.first(), CFGLink.LinkType.DIR_BACK) }
    }
}
data class CFGNodeForStatement      (override val context: Int, override val title: String = "for statement")       : CFGIterationNode(context, title)
data class CFGNodeWhileStatement    (override val context: Int, override val title: String = "while statement")     : CFGIterationNode(context, title)
data class CFGNodeDoWhileStatement  (override val context: Int, override val title: String = "do while statement")  : CFGIterationNode(context, title)
{
    override fun onEnter() {}

    override fun onClose() {
        leaves().forEach { it.link(this) }
        body.add(this)
        link(body.first(), CFGLink.LinkType.DIR_BACK)
    }
}

abstract class CFGJumpNode          (context: Int, title: String = "jump statement", style: NodeStyle = StyleCatalogue.NodeStyles.jump) : CFGNonBodyNode(context, title, style)
data class CFGNodeGotoStatement     (override val context: Int, override val title: String = "goto statement")      : CFGJumpNode(context, title)
data class CFGNodeReturnStatement   (override val context: Int, override val title: String = "return statement")    : CFGJumpNode(context, title)
data class CFGNodeContinueStatement (override val context: Int, override val title: String = "continue statement")  : CFGJumpNode(context, title, StyleCatalogue.NodeStyles.breaks)
data class CFGNodeBreakStatement    (override val context: Int, override val title: String = "break statement")     : CFGJumpNode(context, title, StyleCatalogue.NodeStyles.breaks)

data class CFGNodeFunctionCall      (override val context: Int, override val title: String = "function call")       : CFGNonBodyNode(context, title)
