package ru.er_log.graph.cfg.nodes

import ru.er_log.graph.LinkStyle
import ru.er_log.graph.NodeStyle
import ru.er_log.graph.StyleCatalogue
import ru.er_log.graph.cfg.nodes.nonlinear.CFGJumpNode
import java.util.*
import kotlin.reflect.KClass

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
    val linked: MutableList<CFGLink> = mutableListOf()
) {
    /** Крайний узел, к которому был привязан данный. */
    val lastLinked: CFGNode?
        get() = linked.lastOrNull()?.to

    /**
     * Определяет, может ли текущий узел быть привзяан к [to].
     */
    open fun isLinkable(to: CFGNode): Boolean = true

    open fun link(other: CFGNode, defStyle: LinkStyle? = null, vararg type: CFGLink.LinkType) {
        if (!isLinkable(other)) { return }

        val linkStyle = CFGLink.calculateLinkStyle(defStyle, *type)
        links.add(CFGLink(other, linkStyle))
        other.linked.add(CFGLink(this, linkStyle))
    }

    open fun unlink(other: CFGNode) {
        links.removeIf { it.to == other }
        other.linked.removeIf { it.to == this }
    }

    companion object {
        private var counter = 0
        @Synchronized fun getUID(): Int = counter++
    }
}

/**
 * Узел, который не может содержать в себе других узлов.
 */
abstract class CFGNonBodyNode(
    context: Int,
    deepness: Int,
    title: String,
    style: NodeStyle = StyleCatalogue.NodeStyles.default
) : CFGNode(context, deepness, title, style)

/**
 * Узел, который может содержать в себе другие узлы.
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
        body.add(this)
    }

    /**
     * Вызывается при закрытии данного составного узла.
     * Гарантирует, что после вызова данного метода [body] пополняться не будет.
     */
    open fun onClose() {
    }

    /**
     * Вызывается, когда необходимо вставить элемент [other] внутрь данного узла.
     */
    open fun push(other: CFGNode, linkType: CFGLink.LinkType = CFGLink.LinkType.DEFAULT) {
        val linkTo = when (other) {
            is CFGBodyNode -> other.body.first()
            else -> other
        }

        nodesForLinking().forEach { it.link(linkTo, null, linkType) }
        body.add(other)
    }

    /**
     * Определяет список узлов, с которыми можно связать следующий узел на этой же глубине (в этом же контексте).
     * По умолчанию собирает все листья текущего подграфа. Если таких нет, возвращает последний вставленный элемент.
     * В списке листьев не учитываются [CFGJumpNode].
     *
     * ВНИМАНИЕ:
     *   Эта функция НЕ ДЛЯ ВЫЗОВА из родительских узлов. Допускается вызов только внутри контекста данного узла.
     *   Чтобы изменить поведение сбора листьев также за пределами этого узла, используйте [leaves].
     */
    open fun nodesForLinking(): MutableSet<CFGNode> {
        val leaves = leaves()
        if (leaves.none { it !is CFGJumpNode }) { body.lastOrNull()?.let { leaves.add(it) } }
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
    open fun leaves(): MutableSet<CFGNode> {
        val list = mutableSetOf<CFGNode>()
        body.forEach { node ->
            when(node) {
                is CFGBodyNode -> if (node != this) { list.addAll(node.leaves()) }
                is CFGNonBodyNode -> if (node.links.none { it.to != node }) { list.add(node) }
            }
        }
        return list
    }

    /**
     * Рекурсивно собирает все узлы заданного типа в каждом из дочерних узлов.
     *
     * @param result возвращаемый список, по умолчанию пустой
     * @return заполненный список [result]
     */
    inline fun <reified T : CFGNode> collectAllNodes(result: MutableList<T> = mutableListOf())
            = collectAllNodes(T::class, result)

    fun <T : CFGNode> collectAllNodes(type: KClass<T>, result: MutableList<T>): List<T> {
        body.forEach { node ->
            if (type.isInstance(node)) { result.add(node as T) }
            if (node === this) return@forEach
            if (node is CFGBodyNode) { node.collectAllNodes(type, result) }
        }
        return result
    }
}