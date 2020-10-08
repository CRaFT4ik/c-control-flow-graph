package ru.er_log.graph

sealed class Style(val color: StyleCatalogue.ColorPalette)

class LinkStyle(color: StyleCatalogue.ColorPalette, val style: StyleCatalogue.LinkStyles.Style): Style(color)
class NodeStyle(color: StyleCatalogue.ColorPalette, val shape: StyleCatalogue.NodeStyles.Shape): Style(color)

class StyleCatalogue
{
    enum class ColorPalette(val value: String)
    {
        DARK    ("#404040"),
        GREY    ("#50514F"),
        RED     ("#F25F5C"),
        YELLOW  ("#FFE066"),
        BLUE    ("#247BA0"),
        GREEN   ("#70C1B3"),
    }

    class LinkStyles
    {
        enum class Style(val value: String) {
            SOLID("solid"),
            DOTTED("dotted"),
            DASHED("dashed"),
        }

        companion object {
            val default = LinkStyle(ColorPalette.DARK, Style.SOLID)
        }
    }

    class NodeStyles
    {
        enum class Shape(val value: String) {
            EGG("egg")
        }

        companion object {
            val default = NodeStyle(ColorPalette.GREY, Shape.EGG)
        }
    }
}