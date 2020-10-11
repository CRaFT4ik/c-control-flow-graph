package ru.er_log.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class NetworkManager private constructor()
{
    companion object {
        val instance = NetworkManager()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl("https://www.er-log.ru")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val serviceGraphVis: GraphVisService = retrofit.create(GraphVisService::class.java)
}