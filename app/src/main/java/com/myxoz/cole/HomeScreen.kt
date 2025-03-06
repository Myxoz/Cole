package com.myxoz.cole

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(applicationContext: Context, api: API, prefs: SharedPreferences, openSubScreen: (SubScreen) -> Unit) {
    var content by remember { mutableStateOf(prefs.getString(SPK.HOME, null)?.getAsHomeScreen()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var lastRefreshedTs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(lastRefreshedTs) {
        val fetchedContent = api.getHomeScreenContent()
        isRefreshing=false
        if(fetchedContent==null){
            toastOnMain(applicationContext,"Themen und All-Time stats können nicht abgerufen werden", Toast.LENGTH_LONG)
        } else {
            content = fetchedContent
            prefs.edit().putString(SPK.HOME, fetchedContent.json()).apply()
        }
    }
    Scaffold(
        containerColor = Colors.BACK
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing,
            {isRefreshing=true; lastRefreshedTs=System.currentTimeMillis()},
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .width(500.dp)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text("All-Time", style = MaterialTheme.typography.headlineSmall.copy(Colors.FONT))
                    Leaderboard(content?.topPeople)
                    Spacer(Modifier)
                    val subContent = content
                    subContent?.topics?.forEach {
                        TopicBoard(it,openSubScreen)
                    }
                }
            }
        }
    }
}

fun String?.getAsHomeScreen(): HomeScreen?{
    val json = this?.json?:return null
    println(this)
    return HomeScreen(
        json.getJSONArray("topics").jsonObjArray.map {
            SummedTopic(it.getInt("total"), it.getJSONArray("top").jsonObjArray.map {
                ScoredPerson(it.getString("full"), it.getString("short"), it.getInt("score"))
            }, it.getString("name"), it.getInt("id"))
        },
        json.getJSONArray("top").jsonObjArray.map { ScoredPerson(it.getString("full"), it.getString("short"), it.getInt("score")) }
    )
}
class HomeScreen(val topics: List<SummedTopic>, val topPeople: List<ScoredPerson>): JSONAble() {
    override fun json(): String {
        return JSONObject()
            .put("topics", topics.map { it.json().json }.asJSONArray())
            .put("top",topPeople.map { it.json().json }.asJSONArray())
            .toString()
    }
}