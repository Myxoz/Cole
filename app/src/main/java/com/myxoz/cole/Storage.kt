package com.myxoz.cole

import org.json.JSONArray
import org.json.JSONObject

class SPK {
    companion object {
        fun getContentKey(id: Int): String = "CONTENT_$id"
        const val FULL: String = "FULL"
        const val ID: String = "ID"
        const val SHORT: String = "SHORT"
        const val TOKEN = "TOKEN"
        const val SUBSCREEN = "SUBSCREEN"
        const val HOME = "HOME"
    }
}
class SummedTopic(val totalScore: Int, val topPeople: List<ScoredPerson>, val name: String, val id: Int, val totalTime: Int): JSONAble() {
    override fun json(): String {
        return JSONObject()
            .put("total", totalScore)
            .put("top", topPeople.map { it.json().json }.asJSONArray())
            .put("id",id)
            .put("name",name)
            .put("totalTime",totalTime)
            .toString()
    }
}

open class Person(val full: String, val short: String): JSONAble() {
    override fun json(): String {
        return JSONObject().put("full",full).put("short",short).toString()
    }
}

class ScoredPerson(full: String, short: String, val score: Int, val totalTime: Int):  Person(full, short) {
    override fun json(): String {
        return JSONObject().put("full", full).put("short", short).put("score", score).put("total", totalTime).toString()
    }
}
fun List<JSONObject>.asJSONArray(): JSONArray{
    return JSONArray(this)
}
abstract class JSONAble {
    abstract fun json(): String
}
val String.json: JSONObject
    get() = JSONObject(this)
val JSONArray.jsonObjArray: List<JSONObject>
    get() = this.let {
        val retList = mutableListOf<JSONObject>()
        for(i in 0..<it.length()) {
            retList.add(it.getJSONObject(i))
        }
        retList
    }
