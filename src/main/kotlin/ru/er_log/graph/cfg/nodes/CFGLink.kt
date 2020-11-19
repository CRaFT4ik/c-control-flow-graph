package ru.er_log.graph.cfg.nodes

import ru.er_log.graph.LinkStyle
import ru.er_log.graph.StyleCatalogue

class CFGLink(val to: CFGNode, val style: LinkStyle)
{
    enum class LinkType(val flag: Int)
    {
        DEFAULT     (0b000000),
        NONLINEAR   (0b000001),

        DIR_BACK    (0b000010),
        DIR_JUMP    (0b000100),
        DIR_PRIM    (0b001000),
        DIR_ALTER   (0b010000);

        companion object {
            fun fold(vararg v: LinkType): Int = v.fold(0) { acc, it -> it.flag or acc }
        }
    }

    companion object {
        fun calculateLinkStyle(style: LinkStyle? = null, vararg types: LinkType) : LinkStyle {
            val type = LinkType.fold(*types)

            val lineStyle = when {
                0 != type and LinkType.NONLINEAR.flag -> StyleCatalogue.LinkStyles.Style.DASHED
                else -> StyleCatalogue.LinkStyles.Style.SOLID
            }

            val colorStyle = when {
                0 != type and LinkType.DIR_PRIM.flag -> StyleCatalogue.ColorPalette.GREEN
//                0 != type and LinkType.DIR_ALTER.flag -> StyleCatalogue.ColorPalette.RED
//                0 != type and LinkType.DIR_JUMP.flag -> StyleCatalogue.ColorPalette.RED
//                0 != type and LinkType.DIR_BACK.flag -> StyleCatalogue.ColorPalette.BLUE
                style != null -> style.color
                else -> StyleCatalogue.ColorPalette.GREY
            }

            return LinkStyle(colorStyle, lineStyle)
        }
    }
}