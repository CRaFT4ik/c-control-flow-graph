package ru.er_log.graph.cfg

import ru.er_log.graph.LinkStyle
import ru.er_log.graph.NodeStyle
import ru.er_log.graph.StyleCatalogue
import java.util.*

class Link(val to: CFGNode, val style: LinkStyle)
{
    enum class LinkType(val flag: Int)
    {
        DEFAULT     (0b000000),
        BACKWARD    (0b000001),
        NONLINEAR   (0b000010);

        companion object {
            fun fold(vararg v: LinkType): Int = v.fold(0) { acc, it -> it.flag or acc }
        }
    }
}

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
        val links: MutableList<Link> = mutableListOf(),

        /** Список узлов, которые ссылаются на данный узел. */
        private val linked: Stack<Link> = Stack()
) {
    /** Крайний узел, к которому был привязан данный. */
    val lastLinked: CFGNode?
        get() = linked.lastOrNull()?.to

    fun link(other: CFGNode, vararg type: Link.LinkType) {
        val style = calculateLinkStyle(*type)
        links.add(Link(other, style))
        other.linked.add(Link(this, style))
    }

    private fun calculateLinkStyle(vararg types: Link.LinkType) : LinkStyle {
        val type = Link.LinkType.fold(*types)

        val lineStyle = when {
            0 != type and Link.LinkType.NONLINEAR.flag -> StyleCatalogue.LinkStyles.Style.DOTTED
            else -> StyleCatalogue.LinkStyles.Style.SOLID
        }

        val colorStyle = when {
            0 != type and Link.LinkType.BACKWARD.flag -> StyleCatalogue.ColorPalette.RED
            else -> StyleCatalogue.ColorPalette.DARK
        }

        return LinkStyle(colorStyle, lineStyle)
    }

//    fun unlink(other: CFGNode) {
//        links.remove(other)
//        other.linked.remove(this)
//    }

    companion object {
        private var counter = 0
        @Synchronized fun getUID(): Int = counter++
    }
}

/**
 * Узел с нелинейным выполнением.
 */
abstract class CFGNonLinearNode(context: Int, title: String) : CFGNode(context, title)

/**
 * Узел с линейным выполнением.
 */
abstract class CFGLinearNode(context: Int, title: String): CFGNode(context, title)
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

data class CFGNodeFunction          (override val context: Int, override val title: String = "function")            : CFGNode(context, title)

data class CFGNodeIfStatement       (override val context: Int, override val title: String = "if statement")        : CFGLinearNode(context, title)
data class CFGNodeElseIfStatement   (override val context: Int, override val title: String = "else if statement")   : CFGLinearNode(context, title)
data class CFGNodeElseStatement     (override val context: Int, override val title: String = "else statement")      : CFGLinearNode(context, title)

abstract class CFGIterationNode     (context: Int, title: String) : CFGLinearNode(context, title)
data class CFGNodeForStatement      (override val context: Int, override val title: String = "for statement")       : CFGIterationNode(context, title)
data class CFGNodeWhileStatement    (override val context: Int, override val title: String = "while statement")     : CFGIterationNode(context, title)
data class CFGNodeDoWhileStatement  (override val context: Int, override val title: String = "do while statement")  : CFGIterationNode(context, title)

data class CFGNodeFunctionCall      (override val context: Int, override val title: String = "function call")       : CFGNonLinearNode(context, title)
data class CFGNodeJumpStatement     (override val context: Int, override val title: String = "jump statement")      : CFGNonLinearNode(context, title)
