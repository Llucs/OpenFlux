package com.openflux.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openflux.app.model.TokenUsage
import com.openflux.app.ui.theme.AccentGreen
import com.openflux.app.ui.theme.DarkGray
import com.openflux.app.ui.theme.ErrorRed
import com.openflux.app.ui.theme.LightGray
import com.openflux.app.ui.theme.MediumGray
import com.openflux.app.ui.theme.WarningYellow

@Composable
fun TokenDisplay(
    usage: TokenUsage,
    modifier: Modifier = Modifier
) {
    val progress = usage.percentage.coerceIn(0f, 1f)
    val progressColor = when {
        progress > 0.9f -> ErrorRed
        progress > 0.7f -> WarningYellow
        else -> AccentGreen
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tokens",
                color = LightGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${usage.totalTokens} / ${TokenUsage.MAX_CONTEXT_TOKENS}",
                color = LightGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = progressColor,
            trackColor = DarkGray
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${usage.promptTokens} prompt / ${usage.completionTokens} completion",
                color = MediumGray,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${usage.remainingTokens} remaining",
                color = if (usage.remainingTokens < 10000) ErrorRed else MediumGray,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
