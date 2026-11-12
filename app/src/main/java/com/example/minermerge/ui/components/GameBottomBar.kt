package com.example.minermerge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minermerge.ui.theme.DiamondCyan
import com.example.minermerge.ui.theme.EmeraldGreen
import com.example.minermerge.ui.theme.GoldYellow

@Composable
fun GameBottomBar(
    coins: Int,
    totalDrops: Int,
    onBuyClick: () -> Unit,
    onDropClick: () -> Unit,
    isDropEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                )
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Coins display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "💰 $coins",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.testTag("txt_coins")
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Drops hint
        when {
            totalDrops < 15 -> {
                Text(
                    text = "${15 - totalDrops} drops until diamonds...",
                    color = DiamondCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            totalDrops < 25 -> {
                Text(
                    text = "${25 - totalDrops} drops until emeralds...",
                    color = EmeraldGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            else -> {
                Text(
                    text = "✨ Emeralds Unlocked!",
                    color = EmeraldGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RetroButton(
                text = "BUY",
                onClick = onBuyClick,
                enabled = coins >= 50,
                isGreen = true,
                modifier = Modifier
                    .width(130.dp)
                    .height(44.dp),
                testTag = "btn_buy"
            )

            Spacer(modifier = Modifier.width(12.dp))

            RetroButton(
                text = "DROP",
                onClick = onDropClick,
                enabled = isDropEnabled,
                isGreen = true,
                modifier = Modifier
                    .width(130.dp)
                    .height(44.dp),
                testTag = "btn_drop"
            )
        }
    }
}
