package ru.er_log.cfg

import com.github.aakira.napier.Napier
import okhttp3.ResponseBody
import retrofit2.Response
import ru.er_log.antlr.ANTLRManager
import ru.er_log.network.NetworkManager
import java.io.InputStream


class CFGManager(input: String)
{
    private val graph: CFGraph = CFGraph()
    private val listener = CFGListener(graph)
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

        fun buildTitle(node: CFGNode) {
            builder.append("\"${nodes.indexOf(node)}")
            if (node.title.isNotBlank()) { builder.append("\\n").append(node.title) }
            builder.append("\"")
        }

        builder.append("digraph{")

        nodes.forEach { node ->
            buildTitle(node)
            builder.append("->{")
            node.links.forEach { linkNode ->
                buildTitle(linkNode)
                builder.append(";")
            }
            builder.append("}")
        }

        builder.append("}")
        return builder.toString()
    }
}