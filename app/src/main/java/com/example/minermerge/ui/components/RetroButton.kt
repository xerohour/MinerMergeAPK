package com.example.minermerge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minermerge.ui.theme.ButtonGreenBorder
import com.example.minermerge.ui.theme.ButtonGreenBottom
import com.example.minermerge.ui.theme.ButtonGreenTop
import com.example.minermerge.ui.theme.ButtonGreyBorder
import com.example.minermerge.ui.theme.ButtonGreyBottom
import com.example.minermerge.ui.theme.ButtonGreyTop

@Composable
fun RetroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGreen: Boolean = false,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val topColor = if (isGreen) ButtonGreenTop else ButtonGreyTop
    val bottomColor = if (isGreen) ButtonGreenBottom else ButtonGreyBottom
    val borderColor = if (isGreen) ButtonGreenBorder else ButtonGreyBorder
    val textColor = if (enabled) Color.White else Color(0xFFB0B0B0)

    val shape = RoundedCornerShape(4.dp)

    Box(
        modifier = modifier
            .shadow(4.dp, shape)
            .border(2.dp, borderColor, shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (enabled) listOf(topColor, bottomColor)
                    else listOf(Color(0xFF555555), Color(0xFF333333))
                ),
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
