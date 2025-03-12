package com.myxoz.cole

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Calendar
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.roundToInt

val endedMap = listOf(
    Pair(0, "Jetzt"),
    Pair(10, "vor 10min"),
    Pair(30, "vor 30min"),
    Pair(60, "vor 1h"),
    Pair(90, "vor 1 ½h"),
    Pair(120, "vor 2h"),
    Pair(180, "vor 3h"),
    Pair(60*4, "vor 4h"),
    Pair(60*6, "vor 6h"),
    Pair(60*9, "vor 9h"),
    Pair(60*12, "vor 12h"),
    Pair(60*18, "vor 18h"),
    Pair(60*24, "vor 1d"),
    Pair(60*36, "vor 1 ½d"),
    Pair(60*48, "vor 2d"),
)
val weekDays = listOf("Samstag", "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreen(context: Context, short: String, full: String, api: API, prefs: SharedPreferences, subScreen: SubScreen, closeSubScreen: ()->Unit){
    var content by remember { mutableStateOf(prefs.getString(SPK.getContentKey(subScreen.id), null)?.getAsTopicContent()) }
    var addScreen by remember { mutableStateOf(TopicEntry(
        short,full,"",4,8,System.currentTimeMillis()
    )) }
    var lastTimeAdded by remember { mutableStateOf(0L) }
    var isSending by remember { mutableStateOf(false) }
    var isAddScreenVisible by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }
    LaunchedEffect(subScreen.id, lastTimeAdded) {
        val fetchedContent = api.getTopicContent(subScreen.id)
        if(fetchedContent.status == FetchStatus.OFFLINE) {
            toastOnMain(context, "Offline", Toast.LENGTH_SHORT)
        } else if(
            fetchedContent.status == FetchStatus.FAILED ||
            fetchedContent.status == FetchStatus.STATUS_CODE_NOT_OK ||
            fetchedContent.content == null ||
            !fetchedContent.isJson()
        ) {
            toastOnMain(context, "Thema kann nicht neugeladen werden", Toast.LENGTH_LONG)
        } else {
            content = fetchedContent.content.getAsTopicContent()
            prefs.edit().putString(SPK.getContentKey(subScreen.id), fetchedContent.content).apply()
        }
        isFetching=false
    }
    Scaffold(
        containerColor = Colors.BACK
    ) { paddingValues ->
        PullToRefreshBox(
            isFetching,
            {isFetching=true; lastTimeAdded=System.currentTimeMillis()},
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                Modifier
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .width(500.dp)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier
                            .clip(CircleShape)
                            .clickable {
                                closeSubScreen()
                            }
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "back", tint = Colors.FONT, modifier = Modifier.size(40.dp))
                        Text(subScreen.name, style = MaterialTheme.typography.headlineSmall.copy(Colors.FONT))
                    }
                    val people = content?.entries
                        ?.groupBy { it.short }?.values
                        ?.map { ScoredPerson(it[0].full, it[0].short, it.sumOf { it.getScore() }) }
                        ?.sortedByDescending { it.score }
                    Spacer(Modifier.height(10.dp))
                    Leaderboard(people)
                    val cal = Calendar.getInstance()
                    val currentYear = cal.get(Calendar.YEAR)
                    val groupedByDates = content?.entries
                        ?.sortedByDescending { it.startInMs() }
                        ?.groupBy {
                        cal.timeInMillis=it.startInMs()
                        "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DATE)}"
                    }
                    Column {
                        groupedByDates?.forEach {
                            cal.timeInMillis = it.value[0].startInMs()
                            val dateString =
                                weekDays[cal.get(Calendar.DAY_OF_WEEK)] + " der " +
                                        cal.get(Calendar.DAY_OF_MONTH) +"." + (cal.get(Calendar.MONTH)).plus(1) +
                                        cal.get(Calendar.YEAR).let { itemYear -> if(itemYear==currentYear) "" else ".${itemYear}" }
                            Spacer(Modifier.height(20.dp))
                            Text(
                                dateString,
                                style = MaterialTheme.typography.bodyMedium.copy(Colors.SFONT)
                            )
                            Spacer(Modifier.height(6.dp))
                            Column(
                                Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Colors.SEC),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                it.value.sortedByDescending { it.end }.forEachIndexed { index, item ->
                                    var isExpanded by remember { mutableStateOf(false) }
                                    if(index!=0) HorizontalDivider(
                                        Modifier
                                            .padding(horizontal = 15.dp),
                                        color = Colors.TFONT
                                    )
                                    Surface (
                                        {isExpanded=!isExpanded},
                                        color = Color.Transparent,
                                        shape = RoundedCornerShape(30.dp)
                                    ) {
                                        cal.timeInMillis = item.end * 1000L - item.length * 30L*1000L*60L
                                        Column(
                                            Modifier.padding(15.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    Modifier
                                                        .border(
                                                            4.dp,
                                                            (people?.find { it.short == item.short }?.score
                                                                ?: 0).getColor(
                                                                people?.getOrNull(0)?.score ?: 1
                                                            ),
                                                            CircleShape
                                                        )
                                                        .size(35.dp)
                                                ) {
                                                    Text(
                                                        item.short,
                                                        modifier = Modifier.align(Alignment.Center),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            Colors.FONT
                                                        )
                                                    )
                                                }
                                                Spacer(Modifier.width(15.dp))
                                                Text(
                                                    item.full,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        Colors.FONT
                                                    )
                                                )
                                                Spacer(Modifier.weight(1f))
                                                Text(
                                                    "${
                                                        cal.get(Calendar.HOUR_OF_DAY).toString()
                                                            .padStart(2, '0')
                                                    }:${
                                                        cal.get(Calendar.MINUTE).toString()
                                                            .padStart(2, '0')
                                                    } · ${item.length.asHour()} lang · ${item.productivity}0%",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        Colors.SFONT
                                                    )
                                                )
                                                Spacer(Modifier.width(15.dp))
                                                Text(
                                                    item.getScore().toString(),
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        Colors.FONT
                                                    )
                                                )
                                            }
                                            AnimatedVisibility(
                                                visible = isExpanded,
                                                enter = fadeIn() + expandVertically(),
                                                exit = fadeOut() + shrinkVertically()
                                            ) {
                                                Column(
                                                    Modifier.padding(start = 50.dp)
                                                ) {
                                                    Text(
                                                        "Zusammenfassung:",
                                                        style = MaterialTheme.typography.titleSmall.copy(
                                                            Colors.SFONT
                                                        )
                                                    )
                                                    Text(
                                                        item.summary,
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            Colors.FONT
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
            FloatingActionButton(
                {
                    isAddScreenVisible=!isAddScreenVisible
                },
                Modifier
                    .padding(30.dp)
                    .align(Alignment.BottomEnd)
            ){
                Icon(Icons.Rounded.Add, "add")
            }
            if(isAddScreenVisible){
                ModalBottomSheet(
                    {isAddScreenVisible=false},
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = Colors.BACK,
                ) {
                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 50.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        SliderOption(
                            "Länge",
                            20,
                            1,
                            4,
                            {it.asHour()}
                        ) {
                            addScreen=addScreen.copy(length = it)
                        }
                        SliderOption(
                            "Produktivität",
                            11,
                            0,
                            8,
                            {"${it*10}%"}
                        ) {
                            addScreen=addScreen.copy(productivity = it)
                        }
                        SliderOption(
                            "Aufgehört",
                            endedMap.size,
                            0,
                            0,
                            {
                                endedMap[it].second
                            }
                        ) {
                            addScreen=addScreen.copy(
                                end = System.currentTimeMillis() - endedMap[it].first*1000L*60L
                            )
                            println("${System.currentTimeMillis()} - ${endedMap[it].first*1000L*60L}")
                        }
                        Column(
                            Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ){
                            Text("Zusammenfassung", style = MaterialTheme.typography.titleMedium.copy(Colors.FONT))
                            Row(
                                Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    addScreen.summary,
                                    {addScreen=addScreen.copy(summary = it)},
                                    Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .fillMaxWidth(),
                                    colors = TextFieldDefaults.colors(
                                        unfocusedContainerColor = Colors.SEC,
                                        focusedContainerColor = Colors.SEC,
                                        focusedTextColor = Colors.FONT,
                                        unfocusedTextColor = Colors.FONT,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        imeAction = ImeAction.Done
                                    ),
                                    placeholder = {
                                        Text(
                                            "Knappe Zusammenfassung",
                                            style = MaterialTheme.typography.titleSmall.copy(Colors.SFONT)
                                        )
                                    },
                                    maxLines = 1,
                                )
                            }
                        }
                        Row(
                            Modifier
                                .fillMaxWidth().height(70.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if(isSending){
                                CircularProgressIndicator()
                            } else {
                                FilledTonalButton(
                                    {
                                        isSending=true
                                        CoroutineScope(EmptyCoroutineContext).launch {
                                            val response = api.sendNewEntry(
                                                subScreen,
                                                addScreen
                                            )
                                            isSending=false
                                            if(response) {
                                                lastTimeAdded=System.currentTimeMillis()
                                                toastOnMain(context, "Abgesendet!", Toast.LENGTH_LONG)
                                                isAddScreenVisible=false
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        "Eintragen",
                                        style = MaterialTheme.typography.titleSmall.copy(Color.Black)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier)
                    }
                }
            }
        }
    }
}

class SubScreen(val id: Int, val name: String): JSONAble() {
    override fun json(): String{
        return JSONObject().put("id", id).put("name",name).toString()
    }
}
fun String?.getAsSubScreen(): SubScreen?{
    val json = this?.json?:return null
    return SubScreen(json.getInt("id"), json.getString("name"))
}
fun String?.getAsTopicContent(): TopicContent?{
    val json = this?.json?: return null
    return TopicContent(
        json.getString("name"),
        json.getJSONArray("entries").jsonObjArray.map {
            TopicEntry(
                it.getString("short"),
                it.getString("full"),
                it.getString("summary"),
                it.getInt("length"),
                it.getInt("productivity"),
                it.getLong("end")
            )
        }
    )
}
class TopicContent(val name: String, val entries: List<TopicEntry>): JSONAble() {
    override fun json(): String {
        return JSONObject()
            .put("name", name)
            .put("entries", entries.map { it.json().json }.asJSONArray())
            .toString()
    }
}

class TopicEntry(val short: String, val full: String, val summary: String, val length: Int, val productivity: Int, val end: Long): JSONAble() {
    override fun json(): String {
        return JSONObject()
            .put("short", short)
            .put("full", full)
            .put("summary", summary)
            .put("length", length)
            .put("productivity", productivity)
            .put("end", end)
            .toString()
    }
    fun getScore(): Int = 1*10*length + 3*productivity*length
    fun copy(short: String = this.short, full: String = this.full, summary: String = this.summary, length: Int = this.length, productivity: Int = this.productivity, end: Long = this.end): TopicEntry{
        return TopicEntry(short, full, summary, length, productivity, end)
    }
    fun startInMs() = end*1000 - length*30*60*1000
}
fun Int.asHour(): String {
    return "${if(this>1) this/2 else ""}${if(this%2==1) "${if(this>1) " " else ""}½" else ""}h"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderOption(name: String, steps: Int, start: Int, defVal: Int, displayedValue: (Int)->String, updateValue: (Int) -> Unit){
    val state = remember {
        SliderState(
            defVal.toFloat(),
            steps-2,
            null,
            start.toFloat()..(steps+start-1).toFloat()
        )
            .apply {
                this.onValueChangeFinished={
                    updateValue(this.value.roundToInt())
                }
            }
    }
    Column(
        Modifier
            .fillMaxWidth()
    ){
        Text(name, style = MaterialTheme.typography.titleMedium.copy(Colors.FONT))
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                state,
                Modifier.fillMaxWidth(.8f),
                track = {
                    val percentVal = (it.value-it.valueRange.start)/(it.valueRange.endInclusive-it.valueRange.start)
                    Canvas(Modifier.fillMaxWidth()) {
                        drawLine(Color.Red, Offset(0f, 0f), Offset(percentVal*size.width, 0f), 10f, StrokeCap.Round)
                        drawLine(Colors.SFONT, Offset(percentVal*size.width, 0f), Offset(size.width, 0f), 10f, StrokeCap.Round)
                    }
                },
                thumb = {
                    Box(
                        Modifier.background(Color.Red, CircleShape).size(20.dp)
                    )
                }
            )
            Text(
                displayedValue(state.value.roundToInt()),
                style = MaterialTheme.typography.titleSmall.copy(Colors.FONT)
            )
        }
    }
}