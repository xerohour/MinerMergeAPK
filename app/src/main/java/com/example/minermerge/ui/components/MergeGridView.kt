package com.example.minermerge.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minermerge.R
import com.example.minermerge.model.GiftItem
import com.example.minermerge.model.PickaxeItem
import com.example.minermerge.ui.theme.BrightGold
import com.example.minermerge.ui.theme.SellRed
import com.example.minermerge.ui.theme.SellRedBg
import kotlin.math.roundToInt

private data class DragState(
    val pickaxe: PickaxeItem,
    val fromCol: Int,
    val fromRow: Int,
    val position: Offset
)

@Composable
fun MergeGridView(
    pickaxes: List<PickaxeItem>,
    gifts: List<GiftItem>,
    selectedSlot: Pair<Int, Int>?,
    onSlotClick: (col: Int, row: Int) -> Unit,
    onGiftClick: (giftId: String) -> Unit,
    onMoveOrMerge: (fromCol: Int, fromRow: Int, toCol: Int, toRow: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gift_pulse"
    )

    val density = LocalDensity.current
    val spacingPx = with(density) { 4.dp.toPx() }
    var gridSize by remember { mutableStateOf(IntSize.Zero) }
    var dragState by remember { mutableStateOf<DragState?>(null) }

    // All six columns share one width; rows are square, so the row pitch
    // equals the column pitch.
    fun cellSizePx(): Float =
        if (gridSize.width == 0) 0f else (gridSize.width - 5 * spacingPx) / 6f

    fun cellAt(position: Offset): Pair<Int, Int>? {
        val cell = cellSizePx()
        if (cell <= 0f) return null
        if (position.x < 0f || position.x > gridSize.width ||
            position.y < 0f || position.y > gridSize.height
        ) return null
        val pitch = cell + spacingPx
        val col = (position.x / pitch).toInt().coerceIn(0, 5)
        val row = (position.y / pitch).toInt().coerceIn(0, 2)
        return col to row
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { gridSize = it.size }
                .pointerInput(pickaxes) {
                    // Drag a pickaxe like the original: it follows the
                    // finger, and releasing it over a cell moves it there,
                    // merges it with a same-tier pickaxe, or sells it over
                    // the SELL column. A plain tap still falls through to
                    // the slots' clickable handlers.
                    detectDragGestures(
                        onDragStart = { offset ->
                            val cell = cellAt(offset)
                            if (cell != null && cell.first in 0..4) {
                                val pickaxe = pickaxes.find {
                                    it.gridCol == cell.first && it.gridRow == cell.second
                                }
                                if (pickaxe != null) {
                                    dragState = DragState(
                                        pickaxe = pickaxe,
                                        fromCol = cell.first,
                                        fromRow = cell.second,
                                        position = offset
                                    )
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragState = dragState?.copy(position = change.position)
                        },
                        onDragEnd = {
                            val dragged = dragState
                            dragState = null
                            if (dragged != null) {
                                val cell = cellAt(dragged.position)
                                if (cell != null && cell != (dragged.fromCol to dragged.fromRow)) {
                                    onMoveOrMerge(
                                        dragged.fromCol,
                                        dragged.fromRow,
                                        cell.first,
                                        cell.second
                                    )
                                }
                            }
                        },
                        onDragCancel = { dragState = null }
                    )
                },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (r in 0 until 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 5 grid columns
                    for (c in 0 until 5) {
                        val pickaxe = pickaxes.find { it.gridCol == c && it.gridRow == r }
                        val gift = gifts.find { it.gridCol == c && it.gridRow == r }
                        val isSelected = selectedSlot == (c to r)

                        GridSlotView(
                            pickaxe = pickaxe,
                            gift = gift,
                            isSelected = isSelected,
                            isDragged = dragState?.pickaxe?.id == pickaxe?.id,
                            pulseScale = pulseScale,
                            onClick = {
                                if (gift != null) {
                                    onGiftClick(gift.id)
                                } else {
                                    onSlotClick(c, r)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .testTag("slot_${c}_${r}")
                        )
                    }

                    // 6th Column: SELL Zone (merged for row 0..2 or row-level)
                    if (r == 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .border(2.dp, SellRed, RoundedCornerShape(4.dp))
                                .background(SellRedBg, RoundedCornerShape(4.dp))
                                .clickable { onSlotClick(5, 0) }
                                .testTag("btn_sell_zone"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Sell Pickaxe",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "SELL",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .border(1.dp, SellRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .background(SellRedBg.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .clickable { onSlotClick(5, r) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$50",
                                color = Color(0xFFFFCCCC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // The dragged pickaxe, floating under the finger like the original
        // (slightly transparent, above everything else).
        dragState?.let { dragged ->
            val cell = cellSizePx()
            if (cell > 0f) {
                Image(
                    painter = painterResource(
                        id = ResourceUtils.getPickaxeDrawable(dragged.pickaxe.type)
                    ),
                    contentDescription = "Dragging Pickaxe Tier ${dragged.pickaxe.type + 1}",
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (dragged.position.x - cell / 2).roundToInt(),
                                (dragged.position.y - cell / 2).roundToInt()
                            )
                        }
                        .size(with(density) { cell.toDp() * 0.85f })
                        .alpha(0.85f)
                )
            }
        }
    }
}

@Composable
fun GridSlotView(
    pickaxe: PickaxeItem?,
    gift: GiftItem?,
    isSelected: Boolean,
    isDragged: Boolean,
    pulseScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) BrightGold else Color.White.copy(alpha = 0.45f)
    val borderWidth = if (isSelected) 3.dp else 1.5.dp
    val bgColor = if (isSelected) BrightGold.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .border(borderWidth, borderColor, RoundedCornerShape(4.dp))
            .background(bgColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (pickaxe != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isDragged) 0.3f else 1f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = ResourceUtils.getPickaxeDrawable(pickaxe.type)),
                    contentDescription = "Pickaxe Tier ${pickaxe.type + 1}",
                    modifier = Modifier.fillMaxSize(0.85f)
                )

                // Tier badge at bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${pickaxe.type + 1}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Health dot/badge at top left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                ) {
                    Text(
                        text = "♥${pickaxe.health}",
                        color = Color(0xFFFF5252),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else if (gift != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_gift_box),
                    contentDescription = "Lucky Gift",
                    modifier = Modifier.fillMaxSize(0.8f)
                )
            }
        }
    }
}
