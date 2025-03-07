package com.myxoz.cole

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class API(val token: String, val id: Int) {
    suspend fun getHomeScreenContent(): FetchResult {
        return fetch(getURLByMethod("homescreen"))
    }
    private fun getURLByMethod(method: String, others: String?=null): String{
        return ENV.API_ROOT+"token=${ENV.getOTP(token)}&id=$id&method=${method}"+others.let { if(it!=null) "&$it" else "" }
    }
    suspend fun getTopicContent(id: Int): FetchResult {
        return fetch(getURLByMethod("topic", "topic=$id"))
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
        return received.content=="Done"
    }

    companion object {
        private fun e(url: String) = URLEncoder.encode(url, "UTF-8")
        suspend fun getToken(name: String, short: String): FetchResult {
            return fetch(ENV.API_ROOT+"name=${e(name)}&short=${e(short)}&method=create")
        }
        private suspend fun fetch(url: String): FetchResult {
            var response: String? = null
            var responseCode: FetchStatus
            withContext(Dispatchers.IO){
                println("Fetching: $url")
                try {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.doOutput = true

                    val code = connection.responseCode
                    responseCode = if (code == HttpURLConnection.HTTP_OK) {
                        FetchStatus.OK
                    } else {
                        FetchStatus.STATUS_CODE_NOT_OK
                    }
                    response = connection.inputStream.bufferedReader().use { it.readText() }  // Still Read try
                    connection.disconnect()
                } catch (ex: Exception){
                    println(
                        ex.stackTraceToString()
                    )
                    if(ex is SSLHandshakeException || ex is UnknownHostException){
                        responseCode=FetchStatus.OFFLINE
                    } else {
                        responseCode= FetchStatus.FAILED
                    }
                }
            }
            return FetchResult(response, responseCode)
        }

    }

}
class FetchResult(val content: String?, val status: FetchStatus){
    fun isJson(): Boolean{
        if(content==null) return false;
        return try {
            JSONObject(content)
            true
        } catch (e: JSONException){
            try {
                JSONArray(content)
                true
            } catch (e: JSONException){
                false
            }
        }
    }
}
enum class FetchStatus{
    OK,
    FAILED,
    OFFLINE,
    STATUS_CODE_NOT_OK
}
