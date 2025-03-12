package com.myxoz.cole

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun RegisterScreen(context: Context, updateAfterLogin: (String, String, Int, String) -> Unit) {
    var loginFromOtherDevice by remember { mutableStateOf(false) }
    if(loginFromOtherDevice)
        OtherDeviceLogin(context, updateAfterLogin){loginFromOtherDevice=false}
    else
        Scaffold(containerColor = Colors.BACK){ paddingValues ->
            Spacer(Modifier)
            Column(
                Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                var nameValue by remember { mutableStateOf("") }
                var shortValue by remember { mutableStateOf("") }
                var wasShortValueEdited by remember { mutableStateOf(false) }
                var isFetching by remember { mutableStateOf(false) }
                val textFieldColors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Colors.FONT,
                    unfocusedTextColor = Colors.FONT
                )
                Spacer(Modifier)
                Text(
                    "Wilkommen!",
                    style = MaterialTheme.typography.headlineMedium.copy(color = Colors.FONT)
                )
                Text(
                    "Mehr als deinen Namen und Kürzel einzugeben, ist nicht zu tun",
                    style = MaterialTheme.typography.titleMedium.copy(color = Colors.SFONT),
                    textAlign = TextAlign.Center
                )
                TextField(
                    nameValue,
                    {
                        nameValue=it
                        if(!wasShortValueEdited) {
                            shortValue = (if(nameValue.length < 3) nameValue else if((nameValue.getOrNull(2) ?: 'l') in "aeioue".toCharArray()){
                                it.substring(0..2)
                            } else {
                                it.substring(0..1)
                            }).let { if(it.isEmpty()) "" else (it.getOrNull(0)?.toString()?:"").uppercase()+it.substring(1)}
                        }
                    },
                    placeholder = {
                        Text("Name")
                    },
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done)
                )
                TextField(
                    shortValue,
                    {shortValue=it; wasShortValueEdited=true},
                    placeholder = {
                        Text("Kurzform (2-3 Buchstaben)")
                    },
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done)
                )
                if(isFetching) {
                    CircularProgressIndicator()
                } else {
                    if(nameValue.isNotEmpty() && shortValue.isNotEmpty()){
                        FilledTonalButton(
                            {
                                isFetching=true
                                CoroutineScope(EmptyCoroutineContext).launch {
                                    val response = API.getToken(nameValue, shortValue)
                                    if(response.status==FetchStatus.OFFLINE) {
                                        toastOnMain(context, "Offline", Toast.LENGTH_SHORT)
                                    } else if(response.content=="Already exists") {
                                        toastOnMain(context, "Diese Kurzform hat bereits jemand anderes", Toast.LENGTH_LONG)
                                    } else if(
                                        response.status==FetchStatus.FAILED ||
                                        response.status == FetchStatus.STATUS_CODE_NOT_OK ||
                                        response.content == null ||
                                        response.content.length !in 17..20
                                    ) {
                                        toastOnMain(context, "Etwas ist schiefgelaufen, versuche es später erneut", Toast.LENGTH_LONG)
                                    } else {
                                        updateAfterLogin(response.content.substringBefore(";"), nameValue, response.content.substringAfter(";").toIntOrNull()?:-1, shortValue)
                                    }
                                    isFetching = false
                                }
                            },
                            enabled = true
                        ) {
                            Text("Einloggen")
                        }
                    } else {
                        Text(
                            "Bereits auf einem anderen Gerät angemeldet",
                            style = MaterialTheme.typography.titleSmall.copy(Colors.SFONT, textDecoration = TextDecoration.Underline),
                            modifier = Modifier
                                .clickable {
                                    loginFromOtherDevice = true
                                }
                        )
                    }
                }
            }
        }
}

@Composable
fun OtherDeviceLogin(context: Context, updateAfterLogin: (String, String, Int, String) -> Unit, leaveScreen: ()->Unit) {
    Scaffold(containerColor = Colors.BACK) { padVal ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padVal),
            verticalArrangement = Arrangement.spacedBy(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var deviceCode by remember { mutableStateOf("") }
            var isFetching by remember { mutableStateOf(false) }
            Text(
                "Gehe auf deinem anderen Gerät auf die drei Punkte auf der Startseite und klicke auf \"Anderes Gerät anmelden\"",
                Modifier
                    .width(400.dp)
                    .fillMaxWidth(.9f),
                style = MaterialTheme.typography.titleSmall.copy(Colors.FONT),
                textAlign = TextAlign.Center
            )
            TextField(
                deviceCode,
                {deviceCode=it},
                placeholder = {
                    Text("Code von deinem Gerät", style = MaterialTheme.typography.titleMedium.copy())
                },
                colors = TextFieldDefaults.colors(
                    unfocusedPlaceholderColor = Colors.SFONT,
                    focusedPlaceholderColor = Colors.SFONT,
                    focusedContainerColor = Colors.SEC,
                    unfocusedContainerColor = Colors.SEC,
                    focusedTextColor = Colors.FONT,
                    unfocusedTextColor = Colors.FONT
                )
            )
            if(!isFetching){
                FilledTonalButton({
                    CoroutineScope(EmptyCoroutineContext).launch {
                        isFetching=true
                        val tokenResponse = API.getLoginToken(deviceCode)
                        if(tokenResponse.status!=FetchStatus.OK && tokenResponse.status!=FetchStatus.STATUS_CODE_NOT_OK){
                            fetchToast(context, tokenResponse.status, "Kann nicht eingelogt werden")
                        } else {
                            val content = tokenResponse.content!!
                            if(tokenResponse.isJson()) {
                                val json = content.json
                                updateAfterLogin(json.getString("token"),json.getString("name"),json.getInt("id"), json.getString("shortName"))
                            } else {
                                toastOnMain(context, "Falscher Code", Toast.LENGTH_LONG)
                            }
                        }
                        isFetching=false
                    }
                }) { Text("Anmelden")}
            } else {
                CircularProgressIndicator()
            }
            TextButton ({leaveScreen()}) { Text("Zurück zur Anmeldung")}
        }
    }
}

suspend fun toastOnMain(context: Context, s: String, length: Int) {
    withContext(Dispatchers.Main) {
        Toast.makeText(context,s, length).show()
    }
}
suspend fun fetchToast(context: Context, status: FetchStatus, failedMSG: String){
    val message = when(status){
        FetchStatus.OK -> return
        FetchStatus.FAILED -> failedMSG
        FetchStatus.OFFLINE -> "Offline"
        FetchStatus.STATUS_CODE_NOT_OK -> return
    }
    toastOnMain(context, message, Toast.LENGTH_LONG)
}