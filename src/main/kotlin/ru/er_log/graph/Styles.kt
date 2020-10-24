package ru.er_log.graph

sealed class Style(val color: StyleCatalogue.ColorPalette)

class LinkStyle(color: StyleCatalogue.ColorPalette, val style: StyleCatalogue.LinkStyles.Style): Style(color)
class NodeStyle(fontcolor: StyleCatalogue.ColorPalette, val fillcolor: StyleCatalogue.ColorPalette, val shape: StyleCatalogue.NodeStyles.Shape): Style(fontcolor)

class StyleCatalogue
{
    enum class ColorPalette(val value: String)
    {
        DARK    ("#2D2D2D"),
        GREY    ("#50514F"),
        RED     ("#F26E6B"),
        YELLOW  ("#FFE893"),
        BLUE    ("#85BEEF"),
        GREEN   ("#70C1B3"),
        LIGHT   ("#F5F5F5"),
        LIGHT_D ("#D8D8D8"),
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
            val default         = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.LIGHT, Shape.BOX)
            val function        = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.GREEN, Shape.BOX)
            val functionEnd     = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.RED, Shape.BOX)
            val choice          = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.LIGHT, Shape.DIAMOND)
            val choiceInCycle   = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.BLUE, Shape.DIAMOND)
            val iteration       = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.BLUE, Shape.BOX)
            val jump            = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.RED, Shape.BOX)
            val breaks          = NodeStyle(fontcolor = ColorPalette.DARK, fillcolor = ColorPalette.YELLOW, Shape.BOX)
        }
    }
}