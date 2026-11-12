package com.example.minermerge.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minermerge.R
import com.example.minermerge.model.BlockItem
import com.example.minermerge.model.BlockType
import com.example.minermerge.model.FloatingCoin
import com.example.minermerge.model.GamePhase
import com.example.minermerge.model.PickaxeItem
import com.example.minermerge.ui.theme.BrightGold
import kotlin.math.roundToInt

@Composable
fun MineShaftView(
    blocks: List<BlockItem>,
    fallingPickaxes: List<PickaxeItem>,
    floatingCoins: List<FloatingCoin>,
    cameraScrollY: Float,
    phase: GamePhase,
    scrollMode: Boolean,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        val totalWidth = maxWidth
        val blockSizeDp = totalWidth / 5f
        val blockSizePx = 64f // standard physics size

        // Background wallpaper parallax
        Image(
            painter = painterResource(id = R.drawable.bg_wallpaper),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.45f
        )

        val scrollOffsetY = if (phase == GamePhase.DROPPING) cameraScrollY else 0f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, -scrollOffsetY.roundToInt()) }
        ) {
            // Render Blocks
            for (block in blocks) {
                if (block.isDigged) continue

                val xPos = (block.col * blockSizeDp.value).dp
                val yPos = (120f + block.row * blockSizeDp.value).dp

                Box(
                    modifier = Modifier
                        .offset(x = xPos, y = yPos)
                        .size(blockSizeDp),
                    contentAlignment = Alignment.Center
                ) {
                    // Block Texture
                    Image(
                        painter = painterResource(id = ResourceUtils.getBlockDrawable(block.type)),
                        contentDescription = block.type.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    // Crack overlay
                    val crackRes = ResourceUtils.getCrackDrawable(block.digStage)
                    if (crackRes != null) {
                        Image(
                            painter = painterResource(id = crackRes),
                            contentDescription = "Crack Stage",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }

                    // Ice block containing frozen pickaxe
                    if (block.type == BlockType.ICE && block.frozenPickaxeTier != null) {
                        Image(
                            painter = painterResource(id = ResourceUtils.getPickaxeDrawable(block.frozenPickaxeTier)),
                            contentDescription = "Frozen Pickaxe",
                            modifier = Modifier.fillMaxSize(0.6f),
                            alpha = 0.75f
                        )
                    }

                    // Bonus overlay
                    if (block.isBonus) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(BrightGold.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "BONUS",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Render Dropping Pickaxes
            if (phase == GamePhase.DROPPING) {
                for (pickaxe in fallingPickaxes) {
                    val scaleFactor = blockSizeDp.value / blockSizePx
                    val px = (pickaxe.x * scaleFactor).dp
                    val py = (pickaxe.y * scaleFactor).dp

                    Box(
                        modifier = Modifier
                            .offset(x = px - (blockSizeDp / 2), y = py - (blockSizeDp / 2))
                            .size(blockSizeDp)
                            .rotate(pickaxe.rotation),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = ResourceUtils.getPickaxeDrawable(pickaxe.type)),
                            contentDescription = "Falling Pickaxe",
                            modifier = Modifier.fillMaxSize(0.85f)
                        )
                    }
                }
            }

            // Render Floating Coin Popups
            for (coin in floatingCoins) {
                val scaleFactor = blockSizeDp.value / blockSizePx
                val cx = (coin.x * scaleFactor).dp
                val cy = (coin.y * scaleFactor).dp

                Box(
                    modifier = Modifier.offset(x = cx - 20.dp, y = cy - 10.dp)
                ) {
                    Text(
                        text = "+${coin.amount}",
                        color = BrightGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
        }
    }
}
