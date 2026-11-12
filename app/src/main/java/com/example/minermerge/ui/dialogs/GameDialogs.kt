package com.example.minermerge.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.minermerge.model.GameFormulas
import com.example.minermerge.model.PickaxeItem
import com.example.minermerge.ui.components.ResourceUtils
import com.example.minermerge.ui.theme.BrightGold
import com.example.minermerge.ui.theme.GoldYellow
import com.example.minermerge.ui.theme.SlateBorder
import com.example.minermerge.ui.theme.SlateCard
import com.example.minermerge.ui.theme.SlateSurface

@Composable
fun UpgradeShopDialog(
    coins: Int,
    luckyLootLevel: Int,
    buyQualityLevel: Int,
    luckyFreqLevel: Int,
    onUpgradeLuckyLoot: () -> Unit,
    onUpgradeBuyQuality: () -> Unit,
    onUpgradeLuckyFreq: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, GoldYellow, RoundedCornerShape(8.dp))
                .background(SlateSurface, RoundedCornerShape(8.dp))
                .padding(16.dp)
                .testTag("dialog_shop")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "UPGRADE SHOP",
                    color = GoldYellow,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                // Lucky Loot
                val lootCost = GameFormulas.upgradeCost(luckyLootLevel)
                UpgradeItemCard(
                    title = "Lucky Loot (Lv. $luckyLootLevel)",
                    desc = "Better pickaxes from lucky blocks.",
                    cost = lootCost,
                    canAfford = coins >= lootCost,
                    buttonColor = Color(0xFF2563EB),
                    onUpgrade = onUpgradeLuckyLoot,
                    testTag = "btn_upgrade_loot"
                )

                // Buy Quality
                val qualityCost = GameFormulas.upgradeCost(buyQualityLevel)
                UpgradeItemCard(
                    title = "Buy Quality (Lv. $buyQualityLevel)",
                    desc = "Higher tier pickaxes when buying.",
                    cost = qualityCost,
                    canAfford = coins >= qualityCost && buyQualityLevel < 25,
                    isMax = buyQualityLevel >= 25,
                    buttonColor = Color(0xFF16A34A),
                    onUpgrade = onUpgradeBuyQuality,
                    testTag = "btn_upgrade_quality"
                )

                // Lucky Frequency
                val freqCost = GameFormulas.upgradeCost(luckyFreqLevel)
                UpgradeItemCard(
                    title = "Lucky Frequency (Lv. $luckyFreqLevel)",
                    desc = "More lucky blocks appear in levels.",
                    cost = freqCost,
                    canAfford = coins >= freqCost,
                    buttonColor = Color(0xFFEFB63F),
                    onUpgrade = onUpgradeLuckyFreq,
                    testTag = "btn_upgrade_freq"
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UpgradeItemCard(
    title: String,
    desc: String,
    cost: Int,
    canAfford: Boolean,
    buttonColor: Color,
    onUpgrade: () -> Unit,
    isMax: Boolean = false,
    testTag: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorder, RoundedCornerShape(4.dp))
            .background(SlateCard, RoundedCornerShape(4.dp))
            .padding(10.dp)
    ) {
        Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(text = desc, color = Color(0xFF9CA3AF), fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = onUpgrade,
            enabled = canAfford && !isMax,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                disabledContainerColor = Color(0xFF4B5563)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        ) {
            Text(
                text = if (isMax) "MAX LEVEL" else "Upgrade ($cost 💰)",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AdminPanelDialog(
    isUnlocked: Boolean,
    codeInput: String,
    onCodeChange: (String) -> Unit,
    onUnlock: () -> Unit,
    onProgressLevel: () -> Unit,
    scrollMode: Boolean,
    onToggleScrollMode: () -> Unit,
    selectedTier: Int,
    onTierChange: (Int) -> Unit,
    onSpawnPickaxe: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                .background(SlateSurface, RoundedCornerShape(8.dp))
                .padding(16.dp)
                .testTag("dialog_admin")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Admin Panel",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!isUnlocked) {
                    Text(
                        text = "Enter access code (hint: 000):",
                        color = Color(0xFFD1D5DB),
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = onCodeChange,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF6B7280)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = onUnlock,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Unlock", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onProgressLevel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Progress Level", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onToggleScrollMode,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (scrollMode) Color(0xFF22C55E) else Color(0xFF4B5563)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (scrollMode) "Scroll Mode: ON" else "Scroll Mode: OFF",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Spawn Pickaxe Tier: ${selectedTier + 1}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Slider(
                        value = selectedTier.toFloat(),
                        onValueChange = { onTierChange(it.toInt()) },
                        valueRange = 0f..24f,
                        steps = 23
                    )

                    Button(
                        onClick = onSpawnPickaxe,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCA8A04)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Spawn Pickaxe", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LevelCompleteDialog(
    coinsEarned: Int,
    onNextLevel: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrightGold, RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .padding(20.dp)
                .testTag("dialog_level_complete")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🎉 Level Complete!",
                    color = BrightGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "+$coinsEarned coins",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "+1 Gift Unwrapped",
                    color = Color(0xFF7DD3FC),
                    fontSize = 14.sp
                )

                Button(
                    onClick = onNextLevel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_next_level")
                ) {
                    Text("Next Level", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun AllDestroyedDialog(
    coinsEarned: Int,
    onTryAgain: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .padding(20.dp)
                .testTag("dialog_all_destroyed")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "All Pickaxes Destroyed!",
                    color = Color(0xFFFF5252),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "You earned $coinsEarned coins",
                    color = Color.White,
                    fontSize = 15.sp
                )

                Button(
                    onClick = onTryAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_try_again")
                ) {
                    Text("Try Again", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun SellConfirmDialog(
    pickaxe: PickaxeItem,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                .background(SlateSurface, RoundedCornerShape(8.dp))
                .padding(16.dp)
                .testTag("dialog_sell_confirm")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Sell Pickaxe?",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Are you sure you want to sell this Tier ${pickaxe.type + 1} pickaxe for 50 💰?",
                    color = Color(0xFFD1D5DB),
                    fontSize = 13.sp
                )

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(SlateCard, RoundedCornerShape(4.dp))
                        .border(1.dp, SlateBorder, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = ResourceUtils.getPickaxeDrawable(pickaxe.type)),
                        contentDescription = "Pickaxe to sell",
                        modifier = Modifier.size(44.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sell", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ResetConfirmDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                .background(SlateSurface, RoundedCornerShape(8.dp))
                .padding(16.dp)
                .testTag("dialog_reset_confirm")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Reset Progress?",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "This will delete all your progress, coins, pickaxes, and restart from level 1.",
                    color = Color(0xFFD1D5DB),
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
