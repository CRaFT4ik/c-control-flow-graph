package ru.er_log.graph.cfg

import ru.er_log.graph.LinkStyle


class CFGLink(val to: CFGNode, val style: LinkStyle)
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