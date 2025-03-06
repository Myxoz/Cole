package com.myxoz.cole

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class API(val token: String, val id: Int) {
    suspend fun getHomeScreenContent(): HomeScreen?{
        val received = fetch(getURLByMethod("homescreen"))
        return received.getAsHomeScreen()
    }
    private fun getURLByMethod(method: String, others: String?=null): String{
        return ENV.API_ROOT+"token=${ENV.getOTP(token)}&id=$id&method=${method}"+others.let { if(it!=null) "&$it" else "" }
    }
    suspend fun getTopicContent(id: Int): TopicContent? {
        val received = fetch(getURLByMethod("topic", "topic=$id"))
        return received?.getAsTopicContent()
    }

    suspend fun sendNewEntry(subScreen: SubScreen, addScreen: TopicEntry): Boolean {
        val received = fetch(
            getURLByMethod(
                "new",
                "topic=${subScreen.id}" +
                        "&length=${addScreen.length}" +
                        "&prod=${addScreen.productivity}" +
                        "&end=${addScreen.end/1000}" + // MYSQL Bullshit
                        "&summary=${e(addScreen.summary)}"
            )
        )
        return received=="Done"
    }

    companion object {
        private fun e(url: String) = URLEncoder.encode(url, "UTF-8")
        suspend fun getToken(name: String, short: String): String? {
            return fetch(ENV.API_ROOT+"name=${e(name)}&short=${e(short)}&method=create")
        }
        private suspend fun fetch(url: String): String?{
            return withContext(Dispatchers.IO){
                println("Fetching: $url")
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.doOutput = true

                val responseCode = connection.responseCode
                val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    println("Critical Error: Not 200 OK response")
                    null
                }
                connection.disconnect()
                response
            }
        }
    }
}