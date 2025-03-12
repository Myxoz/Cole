package com.myxoz.cole

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb

class MainActivity : ComponentActivity() {
    private lateinit var prefs: SharedPreferences
    private var backPressed: (()->Unit)?=null
    private val onBackPressedCallback = object: OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            backPressed?.invoke()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(0xFFFFFF),
            navigationBarStyle = SystemBarStyle.dark(Colors.BACK.toArgb())
        )
        setContent {
            prefs = getSharedPreferences(localClassName, MODE_PRIVATE)
            var privateToken by remember{ mutableStateOf(prefs.getString(SPK.TOKEN, null)) }
            // prefs.edit().putString(SPK.getContentKey(1),null).apply()
            var id by remember { mutableIntStateOf(prefs.getInt(SPK.ID, -1)) }
            var short by remember { mutableStateOf(prefs.getString(SPK.SHORT, null)?:"Err") }
            var full by remember { mutableStateOf(prefs.getString(SPK.FULL, null)?:"Error Person") }
            var subScreen by remember { mutableStateOf(prefs.getString(SPK.SUBSCREEN, null).getAsSubScreen()) }
            var renderedSubScreen by remember { mutableStateOf(subScreen?:SubScreen(-1,"")) }
            val updateAfterLogin = {
                token: String, name: String, idName: Int, shortName: String ->
                    privateToken = token
                    full = name
                    id = idName
                    short = shortName
                    prefs
                        .edit()
                        .putString(SPK.TOKEN, token)
                        .putString(SPK.FULL, full)
                        .putInt(SPK.ID, idName)
                        .putString(SPK.SHORT, short)
                        .apply()
            }
            if(privateToken==null) {
                RegisterScreen(applicationContext, updateAfterLogin)
            } else {
                val homeScreenRefreshSubscription = remember { Subscription<Boolean>() }
                HomeScreen(applicationContext, API(privateToken!!, id), prefs, homeScreenRefreshSubscription) {
                    subScreen = it
                    prefs.edit().putString(SPK.SUBSCREEN, it.json()).apply()
                    renderedSubScreen = it;
                    backPressed = { subScreen = null; backPressed = {} }
                }
                AnimatedVisibility(
                    subScreen!=null,
                    enter = slideInHorizontally() {it} + fadeIn(),
                    exit = slideOutHorizontally() {it} + fadeOut()
                ) {
                    SubScreen(applicationContext, short, full, API(privateToken!!, id), prefs, renderedSubScreen, homeScreenRefreshSubscription) {
                        prefs.edit().putString(SPK.SUBSCREEN, null).apply()
                        subScreen=null
                    }
                }
            }
        }
    }
}