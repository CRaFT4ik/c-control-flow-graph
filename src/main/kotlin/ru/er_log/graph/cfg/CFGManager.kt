package ru.er_log.graph.cfg

import com.github.aakira.napier.Napier
import okhttp3.ResponseBody
import retrofit2.Response
import ru.er_log.antlr.ANTLRManager
import ru.er_log.graph.ast.ASTListener
import ru.er_log.network.NetworkManager
import java.io.InputStream
import kotlin.math.absoluteValue


class CFGManager(input: String)
{
    private val graph: CFGraph = CFGraph()
    private val listener = ASTListener(graph)
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
     * @param writer Long - обще кол-во данных, либо null
     */
    fun toImage(writer: (InputStream, Long?) -> Unit) {
        val call = network.serviceGraphVis.buildImage(graph = toGraph())
        try {
            val response: Response<ResponseBody> = call.execute()
            if (response.isSuccessful && response.body() != null) {
                val total = response.body()!!.contentLength()
                writer(response.body()!!.byteStream(), if (total != -1L) { total } else { null })
            } else {
                throw Exception("Can't get image from server: ${response.raw()}")
            }
        } catch (e: Exception) {
            Napier.e("Error while building graph: ${e.message}")
        }
    }

    fun toGraph(): String {
        val builder = StringBuilder()
        val nodes = graph.graph

        fun color(obj: Any): String {
            val colorPool = listOf(
                "#ECD1C9", "#FBB5AE", "#FFEFBC", "#B7D1DF", "#D1E2CE",
                "#FADAE5", "#ECE3D5", "#F2F2F2", "#ECE3C1", "#BEDFC8",
                "#F9F2B6", "#EFD0BD", "#DDD0E5", "#F2E4C8", "#CBCBCB")
            return colorPool[obj.hashCode().absoluteValue % colorPool.size]
        }

        fun title(node: CFGNode): String {
            val titleBuilder = StringBuilder()
            titleBuilder.append("\"${node.uid}")
            if (node.title.isNotBlank()) { titleBuilder.append("\\n").append(node.title) }
            titleBuilder.append("\"")
            return titleBuilder.toString()
        }

        fun node(node: CFGNode): String {
            return "${title(node)}[shape=\"${node.style.shape.value}\",color=\"${node.style.color.value}\",fillcolor=\"${color(node.context)}\"];"
        }

        builder.append("digraph{")
        builder.append("""node[style="filled",fillcolor="#F2F2F2",fontname=Inter];""")

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

        builder.append("splines=spline;label=\"Control-flow graph by Eldar T.\";}")
        return builder.toString()
    }

//    fun toText() : String {
//        val graph = toGraph()
//                .replace("\n\r", "")
//                .replace("\\n", " : ")
//                .replace("\"", "")
//
//        val split = graph.split('{').filter { it.isNotBlank() }
//
//        val builder = StringBuilder()
//        split.forEach { nodeRaw ->
//            val node = nodeRaw.substringAfter('}')
//            builder.append(node).append('\n')
//
//            val elements = nodeRaw.substringBefore('}', "")
//            val elementsSplit = elements.split(';').filter { it.isNotBlank() }
//            elementsSplit.forEach { builder.append('\t').append(it).append('\n') }
//
//            builder.append('\n')
//        }
//        return builder.toString()
//    }
}