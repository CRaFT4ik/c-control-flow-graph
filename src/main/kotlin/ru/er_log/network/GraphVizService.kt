package ru.er_log.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming


/**
 * API: https://quickchart.io/documentation/graphviz-api/
 */
interface GraphVisService
{
    @GET("https://quickchart.io/graphviz")
    @Streaming
    fun buildImage(
            @Query("format") format: String = "png",
            @Query("graph") graph: String
    ): Call<ResponseBody>
}