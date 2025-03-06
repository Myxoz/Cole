package com.myxoz.cole

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Leaderboard(topPeople: List<ScoredPerson>?) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Colors.SEC, RoundedCornerShape(70.dp))
            .padding(horizontal = 30.dp, vertical = 20.dp)
            .height(300.dp)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        if (topPeople == null) {
            CircularProgressIndicator()
        } else if(topPeople.isEmpty()){
            Text("Keine Einträge",
                style = MaterialTheme.typography.titleLarge.copy(Colors.FONT)
            )
        } else {
            val max = topPeople[0].score
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if(topPeople.size > 1) Tower(topPeople[1], max)
                Tower(topPeople[0], max)
                if(topPeople.size > 2) Tower(topPeople[2], max)
            }
            Spacer(Modifier.height(10.dp))
            if (topPeople.size > 3) {
                Row(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "4. ${topPeople[3].full} • ${topPeople[3].score}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            topPeople[3].score.getColor(max)
                        )
                    )
                    if(topPeople.size > 4) {
                        Text(
                            "5. ${topPeople[4].full} • ${topPeople[4].score}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                topPeople[4].score.getColor(max)
                            )
                        )
                    }
                }
            }
        }
    }
}
fun Int.getColor(max: Int) = if(max==0) Color.White else Color(
    (1-this/max.toFloat()*.9f)+.3f,
    (this/max.toFloat())*.9f+.3f,
    (1-this/max.toFloat())*.2f+.3f,
    1f
)

@Composable
fun RowScope.Tower(person: ScoredPerson, max: Int){
    val color = person.score.getColor(max)
    Column(
        Modifier
            .fillMaxHeight()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Bottom),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(50.dp)
                .border(4.dp, color, CircleShape)
        ) {
            Text(
                person.short,
                Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleLarge.copy(Colors.FONT)
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            person.score.toString(),
            Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge.copy(Colors.FONT)
        )
        Box(
            Modifier
                .fillMaxWidth(.8f)
                .height(
                    (((person.score/max.toFloat())*150).dp)
                )
                .background(color, RoundedCornerShape(25.dp))
        )
    }
}