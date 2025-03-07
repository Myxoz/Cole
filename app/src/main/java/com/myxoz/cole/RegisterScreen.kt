package com.myxoz.cole

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun RegisterScreen(context: Context, updateAfterLogin: (String, String, Int, String) -> Unit) {
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
                    enabled = nameValue.isNotEmpty() && shortValue.isNotEmpty() && !isFetching
                ) {
                    Text("Einloggen")
                }
            }
        }
    }
}

suspend fun toastOnMain(context: Context, s: String, length: Int) {
    withContext(Dispatchers.Main) {
        Toast.makeText(context,s, length).show()
    }
}
