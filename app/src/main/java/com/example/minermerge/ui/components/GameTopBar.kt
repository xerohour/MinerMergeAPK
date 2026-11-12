package com.example.minermerge.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minermerge.R

@Composable
fun GameTopBar(
    onResetClick: () -> Unit,
    onUpgradesClick: () -> Unit,
    onSaveClick: () -> Unit,
    onAdminClick: () -> Unit,
    currentLevel: Int,
    isAdminUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RetroButton(
                text = "reset",
                onClick = onResetClick,
                modifier = Modifier.weight(1f),
                testTag = "btn_reset"
            )
            RetroButton(
                text = "upgrades",
                onClick = onUpgradesClick,
                modifier = Modifier.weight(1.1f),
                testTag = "btn_upgrades"
            )
            RetroButton(
                text = "save",
                onClick = onSaveClick,
                modifier = Modifier.weight(1f),
                testTag = "btn_save"
            )
            RetroButton(
                text = "admin panel",
                onClick = onAdminClick,
                modifier = Modifier.weight(1.2f),
                testTag = "btn_admin"
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo_miner_merge),
            contentDescription = "Miner Merge",
            modifier = Modifier
                .width(220.dp)
                .height(44.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "remix v1.4",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Level $currentLevel",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            if (isAdminUnlocked) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "[admin]",
                    color = Color(0xFFEF4444),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
