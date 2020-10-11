package ru.er_log.graph.cfg.nodes

import com.github.aakira.napier.Napier
import ru.er_log.graph.NodeStyle
import ru.er_log.graph.StyleCatalogue
import java.util.*

sealed class CFGNode(
    /** Контекст (uuid) создания узла.
     * Можно опознать открывающий \ закрывающий узлы. */
    open val context: Int,

    /** Глубина использования узла.
     * 0 - уровень объявления функций, 1 - ее тела, и т.д. */
    open val deepness: Int,

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
        val style = CFGLink.calculateLinkStyle(*type)
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
abstract class CFGNonBodyNode(
    context: Int,
    deepness: Int,
    title: String,
    style: NodeStyle = StyleCatalogue.NodeStyles.default
) : CFGNode(context, deepness, title, style)
{
    /**
     * Определяет, может ли текущий узел быть привзяан к [to].
     */
    open fun linkable(to: CFGNode): Boolean = true

    override fun link(other: CFGNode, vararg type: CFGLink.LinkType) {
        if (linkable(other)) { super.link(other, *type) }
    }
}

/**
 * Узел с линейным выполнением.
 */
abstract class CFGBodyNode(
    context: Int,
    deepness: Int,
    title: String,
    style: NodeStyle = StyleCatalogue.NodeStyles.default
) : CFGNode(context, deepness, title, style)
{
    /**
     * Список вставленных элементов.
     * Должен включать элемент [this].
     */
    val body: Stack<CFGNode> = Stack()

    /**
     * Вызывается при создании данного составного узла.
     */
    open fun onEnter() {
        Napier.i("enter in ${this.title}")
        body.add(this)
    }

    /**
     * Вызывается при закрытии данного составного узла.
     * Гарантирует, что после вызова данного метода [body] пополняться не будет.
     */
    open fun onClose() {
        Napier.w("exit from ${this.title}")
    }

    /**
     * Вызывается, когда необходимо вставить элемент [other] внутрь данного узла.
     */
    open fun push(other: CFGNode, linkType: CFGLink.LinkType = CFGLink.LinkType.DEFAULT) {
        Napier.v("  pushed ${other.title} into ${this.title}")

        val linkTo = when (other) {
            is CFGBodyNode -> other.body.first()
            else -> other
        }

        leaves().forEach { it.link(linkTo, linkType) }
        body.add(other)
    }

    /**
     * Собирает все листья текущего подграфа.
     * Если таких нет, возвращает последний вставленный элемент.
     *
     * ВНИМАНИЕ:
     *   Эта функция НЕ ДЛЯ ВЫЗОВА из родительских узлов. Допускается вызов только внутри контекста данного узла.
     *   Чтобы изменить поведение сбора листьев также за пределами этого узла, используйте [formLeaves].
     */
    open fun leaves(): MutableSet<CFGNode> {
        val leaves = formLeaves()
        if (leaves.isEmpty()) { body.lastOrNull()?.let { leaves.add(it) } }
        return leaves
    }

    /**
     * Собирает все листья текущего подграфа.
     * Если лист является [CFGBodyNode], то к списку добавляются и его листья, и так рекурсивно.
     *
     * ВНИМАНИЕ:
     *   Эта функция вызывается еще и при сборе листьев В ДОЧЕРНИХ блоках, то есть также когда листья
     *   собирает и родительский узел.
     */
    open fun formLeaves(): MutableSet<CFGNode> {
        val list = mutableSetOf<CFGNode>()
        body.forEach { node ->
            when(node) {
                is CFGBodyNode -> if (node != this) { list.addAll(node.formLeaves()) }
                is CFGNonBodyNode -> if (node.links.none { it.to != node }) { list.add(node) }
            }
        }
        return list
    }
}