package ru.er_log.graph.cfg

import com.github.aakira.napier.Napier
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import okhttp3.ResponseBody
import retrofit2.Response
import ru.er_log.antlr.ANTLRManager
import ru.er_log.graph.StyleCatalogue
import ru.er_log.graph.cfg.nodes.CFGNode
import ru.er_log.network.NetworkManager
import java.io.File
import java.io.InputStream
import kotlin.math.absoluteValue

class CFGManager(input: String)
{
    private val graph: CFGraph = CFGraph()
    private val listener = ASTAdapter(graph)
    private val antlrManager: ANTLRManager = ANTLRManager(input, listener)

    fun calculate(): CFGResult {
        antlrManager.run()
        return CFGResult(graph.copy())
    }
}

data class CFGResult(
    val graph: CFGraph
) {
    private val network: NetworkManager = NetworkManager.instance

    /**
     * @param writer Long - общее кол-во данных, либо null
     */
    fun toImageByNetwork(writer: (InputStream, Long?) -> Unit) {
        val call = network.serviceGraphVis.buildImage(graph = toGraph())
        try {
            val response: Response<ResponseBody> = call.execute()
            if (response.isSuccessful && response.body() != null) {
                val total = response.body()!!.contentLength()
                writer(
                    response.body()!!.byteStream(), if (total != -1L) {
                        total
                    } else {
                        null
                    }
                )
            } else {
                throw Exception("Can't get image from server: ${response.raw()}")
            }
        } catch (e: Exception) {
            Napier.e("Error while building graph: ${e.message}")
        }
    }

    fun toImage(file: File) {
        val gv = Graphviz.fromString(toGraph())
        gv.render(Format.PNG).toFile(file)
    }

    fun toGraph(): String {
        val builder = StringBuilder()
        val nodes = graph.graph

        fun color(obj: Any): String {
            val colorPool = listOf(
                "#ECD1C9", "#FBB5AE", "#FFEFBC", "#B7D1DF", "#D1E2CE",
                "#FADAE5", "#ECE3D5", "#F2F2F2", "#ECE3C1", "#BEDFC8",
                "#F9F2B6", "#EFD0BD", "#DDD0E5", "#F2E4C8", "#CBCBCB"
            )
            return colorPool[obj.hashCode().absoluteValue % colorPool.size]
        }

        fun String.normalize(): String {
            return this.replace("\"", "\\\"")
        }

        fun title(node: CFGNode): String {
            val titleBuilder = StringBuilder()
            titleBuilder.append("\"")
            titleBuilder.append(node.uid)
            if (node.title.isNotBlank()) { titleBuilder.append(" :: ").append(node.title.normalize()) }
            titleBuilder.append("\"")
            return titleBuilder.toString()
        }

        fun node(node: CFGNode): String {
            val width = when(node.style.shape) {
                StyleCatalogue.NodeStyles.Shape.DIAMOND -> "4"
                else -> "2"
            }

            val fillColor = if (!node.isDeadCode) { node.style.fillcolor.value } else { StyleCatalogue.ColorPalette.BISQUE.value }
            return "${title(node)}[shape=\"${node.style.shape.value}\",fontcolor=\"${node.style.color.value}\",fillcolor=\"${fillColor}\",width=$width];"
        }

        builder.append("digraph{")
        builder.append("""node[style="filled,rounded",color="#B9B9B9",fillcolor="#F2F2F2",fontname="Arial",height=0.64,margin="0.1,0"];""")

        nodes.forEach { node ->
            node.links.forEach { linkNode ->
                builder.append(node(node))
                builder.append(node(linkNode.to))
                builder.append(title(node))
                builder.append("->")
                builder.append(title(linkNode.to))
                builder.append("[style=\"${linkNode.style.style.value}\",color=\"${linkNode.style.color.value}\"]")
                builder.append(";")
            }
        }

        builder.append("splines=spline;label=\"\\nControl-flow graph by Eldar T.\\n\";}")
        return builder.toString()
    }

    /**
     * TODO: Метод необходимо переписать.
     *
     * @return строковое представление графа
     */
    fun toText() : String {
        val graph = toGraph()
                .replace("\n\r", "")
                .replace("\\n", " : ")
                .replace("\"", "")

        val split = graph.split('{').filter { it.isNotBlank() }

        val builder = StringBuilder()
        split.forEach { nodeRaw ->
            val node = nodeRaw.substringAfter('}')
            builder.append(node).append('\n')

            val elements = nodeRaw.substringBefore('}', "")
            val elementsSplit = elements.split(';').filter { it.isNotBlank() }
            elementsSplit.forEach { builder.append('\t').append(it).append('\n') }

            builder.append('\n')
        }
        return builder.toString()
    }
}