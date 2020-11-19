package ru.er_log.graph

sealed class Style(val color: StyleCatalogue.ColorPalette)

class LinkStyle(color: StyleCatalogue.ColorPalette, val style: StyleCatalogue.LinkStyles.Style): Style(color)
class NodeStyle(fontcolor: StyleCatalogue.ColorPalette, val fillcolor: StyleCatalogue.ColorPalette, val shape: StyleCatalogue.NodeStyles.Shape): Style(fontcolor)

class StyleCatalogue
{
    enum class ColorPalette(val value: String)
    {
        WHITE   ("#FFFFFF"),
        DARK    ("#2D2D2D"),
        GREY    ("#50514F"),
        RED     ("#EB8D8D"),
        YELLOW  ("#FFE893"),
        BISQUE  ("#FFE4C4"),
        BLUE    ("#93C3ED"),
        GREEN   ("#74C2B5"),
        LIGHT   ("#F5F5F5"),
        LIGHT_L ("#DFDFDF"),
        LIGHT_H ("#BEBEBE"),
    }

    class LinkStyles
    {
        enum class Style(val value: String) {
            SOLID("solid"),
            DOTTED("dotted"),
            DASHED("dashed"),
        }

        companion object {
            val default = LinkStyle(ColorPalette.GREY, Style.SOLID)
            val dashed = LinkStyle(ColorPalette.GREY, Style.DASHED)
            val succeed = LinkStyle(ColorPalette.GREEN, Style.SOLID)
        }
    }

    class NodeStyles
    {
        enum class Shape(val value: String) {
            EGG("egg"),
            DIAMOND("diamond"),
            BOX("box"),
        }

        companion object {
            val default         = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.WHITE, Shape.BOX)
            val function        = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.GREEN, Shape.BOX)
            val functionEnd     = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.RED, Shape.BOX)
            val choice          = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.LIGHT, Shape.DIAMOND)
            val choiceInCycle   = choice // NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.BLUE, Shape.DIAMOND)
            val iteration       = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.BLUE, Shape.BOX)
            val jump            = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.RED, Shape.BOX)
            val breaks          = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.YELLOW, Shape.BOX)
        }
    }
}