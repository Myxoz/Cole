package com.myxoz.cole

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TopicBoard(topic: SummedTopic, openSubScreen: (SubScreen) -> Unit) {
    Surface(
        {
            openSubScreen(SubScreen(topic.id, topic.name))
        },
        Modifier
            .fillMaxWidth()
        ,
        color = Colors.SEC,
        shape = RoundedCornerShape(30.dp)
    ) {
        Column(
            Modifier
                .padding(20.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    topic.name,
                    style = MaterialTheme.typography.titleLarge.copy(Colors.FONT)
                )
                Text(
                    topic.totalScore.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(Colors.FONT)
                )
            }
            val max = topic.topPeople.getOrNull(0)?.score?:0
            if(topic.topPeople.isNotEmpty()) {
                Spacer(Modifier.height(5.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ){
                    repeat(3) {
                        topic.topPeople.getOrNull(it)?.apply {
                            Text(
                                "${it+1}. $full · $score",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    score.getColor(max)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

