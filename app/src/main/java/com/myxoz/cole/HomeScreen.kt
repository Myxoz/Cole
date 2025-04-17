package com.myxoz.cole

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(applicationContext: Context, api: API, prefs: SharedPreferences, homeScreenRefreshSubscription: Subscription<Boolean>, openSubScreen: (SubScreen) -> Unit) {
    var codesPopupOpen by remember { mutableStateOf(false) }
    var content by remember { mutableStateOf(prefs.getString(SPK.HOME, null)?.getAsHomeScreen()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var lastRefreshedTs by remember { mutableLongStateOf(0L) }
    homeScreenRefreshSubscription.register("home"){
        lastRefreshedTs=-lastRefreshedTs
    }
    LaunchedEffect(lastRefreshedTs) {
        val fetchedContent = api.getHomeScreenContent()
        isRefreshing=false
        if(fetchedContent.status== FetchStatus.OFFLINE) {
            toastOnMain(applicationContext, "Offline", Toast.LENGTH_SHORT)
        } else if(
            fetchedContent.status== FetchStatus.FAILED ||
            !fetchedContent.isJson()
        ){
            toastOnMain(applicationContext,"Themen und Statistiken können nicht abgerufen werden", Toast.LENGTH_LONG)
        } else if(fetchedContent.content!=null){
            content = fetchedContent.content.getAsHomeScreen()
            prefs.edit().putString(SPK.HOME, fetchedContent.content).apply()
        }
    }
    Scaffold(
        containerColor = Colors.BACK
    ) { paddingValues ->
        if(codesPopupOpen){
            Dialog ({codesPopupOpen=false}) {
                Column(
                    Modifier
                        .background(Colors.SEC, RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    var code by remember { mutableStateOf(ENV.getLoginOTP(api.token, api.id)) }
                    Text("Gib diesen Code auf deinem anderen Gerät ein:", color = Colors.SFONT, textAlign = TextAlign.Center)
                    Text(
                        code,
                        style = MaterialTheme.typography.titleLarge.copy(Colors.FONT)
                    )
                    Text("Gültig für:", color = Colors.SFONT)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        var timeLeft by remember { mutableFloatStateOf(ENV.getOTPValidityDuration()) }
                        LaunchedEffect("loginCodes") {
                            var statedAt = System.currentTimeMillis()
                            var startLeft = ENV.getOTPValidityDuration()/1000f
                            while (true){
                                delay(1000)
                                timeLeft=startLeft-(System.currentTimeMillis()-statedAt)/1000f
                                if(timeLeft<=0){
                                    code = ENV.getLoginOTP(api.token, api.id)
                                    timeLeft = ENV.getOTPValidityDuration()
                                    statedAt = System.currentTimeMillis()
                                    startLeft = ENV.getOTPValidityDuration()
                                }
                            }
                        }
                        LinearProgressIndicator(
                            {
                                timeLeft/90.toFloat()
                            },
                            modifier = Modifier
                                .weight(1f)
                        )
                        Text("${timeLeft.toInt()}s", color = Colors.SFONT)
                    }
                }
            }
        }
        PullToRefreshBox(
            isRefreshing,
            {isRefreshing=true; lastRefreshedTs=System.currentTimeMillis()},
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                Column(
                    Modifier
                        .width(500.dp)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text("All-Time", style = MaterialTheme.typography.titleLarge.copy(Colors.FONT))
                    Leaderboard(content?.topPeople)
                    Spacer(Modifier)
                    val subContent = content
                    subContent?.topics?.forEach {
                        TopicBoard(it,openSubScreen)
                    }
                    Spacer(Modifier)
                }
            }
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
            ) {
                var expanded by remember { mutableStateOf(false) }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Colors.FONT)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = Colors.BACK
                ) {
                    DropdownMenuItem(
                        text = { Text("Anderes Gerät anmelden", color = Colors.FONT) },
                        onClick = {
                            codesPopupOpen = true
                        }
                    )
                }
            }
        }
    }
}

fun String?.getAsHomeScreen(): HomeScreen?{
    val json = this?.json?:return null
    println(this)
    return HomeScreen(
        json.getJSONArray("topics").jsonObjArray.map { topic ->
            SummedTopic(topic.getInt("total"), topic.getJSONArray("top").jsonObjArray.map {
                ScoredPerson(it.getString("full"), it.getString("short"), it.getInt("score"), -1) // Not displayed
            }, topic.getString("name"), topic.getInt("id"), safe{topic.getInt("totalTime")}?:-1) // Needs safe for migration
        },
        json.getJSONArray("top").jsonObjArray.map { ScoredPerson(it.getString("full"), it.getString("short"), it.getInt("score"),safe{it.getInt("total")}?:-1)} // Needed cause the data needs to migrate
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
class Subscription<T>{
    private val subscriptionMap = mutableMapOf<String,(T)->Unit>()
    fun register(key: String, func: (T)->Unit){
        subscriptionMap[key]=func
    }
    fun send(data: T){
        subscriptionMap.forEach { (_, u) -> u(data) }
    }
}
fun <T>safe(func: ()->T): T?{
    return try {
        func()
    } catch (e: Exception) {
        null
    }
}