package com.example.minermerge.model

import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.random.Random

@Serializable
data class PickaxeTierStats(
    val damage: Int,
    val health: Int
)

object PickaxeTiers {
    val STATS = mapOf(
        0 to PickaxeTierStats(1, 9),
        1 to PickaxeTierStats(2, 10),
        2 to PickaxeTierStats(3, 11),
        3 to PickaxeTierStats(4, 12),
        4 to PickaxeTierStats(5, 13),
        5 to PickaxeTierStats(7, 14),
        6 to PickaxeTierStats(8, 15),
        7 to PickaxeTierStats(9, 16),
        8 to PickaxeTierStats(11, 17),
        9 to PickaxeTierStats(12, 18),
        10 to PickaxeTierStats(14, 19),
        11 to PickaxeTierStats(15, 20),
        12 to PickaxeTierStats(17, 21),
        13 to PickaxeTierStats(18, 22),
        14 to PickaxeTierStats(20, 23),
        15 to PickaxeTierStats(22, 24),
        16 to PickaxeTierStats(24, 25),
        17 to PickaxeTierStats(26, 26),
        18 to PickaxeTierStats(28, 27),
        19 to PickaxeTierStats(30, 28),
        20 to PickaxeTierStats(32, 29),
        21 to PickaxeTierStats(34, 30),
        22 to PickaxeTierStats(36, 31),
        23 to PickaxeTierStats(38, 32),
        24 to PickaxeTierStats(40, 33)
    )

    fun getStats(tier: Int): PickaxeTierStats {
        return STATS[tier.coerceIn(0, 24)] ?: PickaxeTierStats(1, 9)
    }
}

@Serializable
data class PickaxeItem(
    val id: String,
    val type: Int,
    var health: Int,
    val maxHealth: Int,
    var gridCol: Int? = null,
    var gridRow: Int? = null,
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var rotation: Float = 0f
)

@Serializable
enum class BlockType(val baseHealth: Int, val baseCoins: Int) {
    GRASS(3, 1),
    DIRT(3, 1),
    COBBLE(9, 3),
    STONE(3, 1),
    COAL(6, 5),
    IRON(9, 10),
    GOLD(15, 20),
    DIAMOND(20, 100),
    EMERALD(25, 250),
    REDSTONE(18, 30),
    LAPIS(16, 25),
    ICE(25, 0),
    TNT(1, 0),
    LUCKY(1, 0),
    CHEST(1, 0)
}

fun calculateBlockHealth(type: BlockType, level: Int): Int {
    if (type == BlockType.LUCKY || type == BlockType.TNT || type == BlockType.CHEST) return 1
    return ceil(type.baseHealth * 1.05.pow((level - 1).toDouble())).toInt()
}

fun calculateBlockCoins(type: BlockType, level: Int): Int {
    if (type == BlockType.CHEST || type == BlockType.ICE || type == BlockType.LUCKY || type == BlockType.TNT) return 0
    return ceil(type.baseCoins * 1.03.pow((level - 1).toDouble())).toInt()
}

@Serializable
data class BlockItem(
    val id: String,
    val col: Int,
    val row: Int,
    val type: BlockType,
    var health: Int,
    val maxHealth: Int,
    var digStage: Int = 0,
    val giftId: String? = null,
    val frozenPickaxeTier: Int? = null,
    val isBonus: Boolean = false,
    var isDigged: Boolean = false,
    var isAnimating: Boolean = false,
    var animationTime: Int = 0,
    var hasPickaxe: Boolean = false
)

@Serializable
data class GiftItem(
    val id: String,
    var gridCol: Int? = null,
    var gridRow: Int? = null
)

data class FloatingCoin(
    val id: String,
    var x: Float,
    var y: Float,
    val amount: Int,
    var age: Int = 0
)

data class GameParticle(
    val id: String,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val color: Long,
    var age: Int = 0,
    val lifetime: Int = 60
)

enum class GamePhase {
    SETUP,
    DROPPING
}

object GameFormulas {
    fun upgradeCost(level: Int): Int {
        return when {
            level == 1 -> 50
            level < 5 -> (level - 1) * 50 + 50
            level < 30 -> (level - 2) * 100
            else -> 3000 + (level - 30) * 1000
        }
    }

    fun getGiftPickaxeTier(level: Int, luckyLootLevel: Int): Int {
        val v = level + (luckyLootLevel - 1)
        val r = (v / 3).coerceAtMost(14)
        val rand = Random.nextFloat()
        return when {
            v <= 2 -> 0
            v <= 5 -> if (rand < 0.7f) r else minOf(14, r + 1)
            v <= 8 -> when {
                rand < 0.5f -> r
                rand < 0.8f -> minOf(14, r + 1)
                else -> minOf(14, r + 2)
            }
            v <= 11 -> when {
                rand < 0.4f -> r
                rand < 0.7f -> minOf(14, r + 1)
                rand < 0.9f -> minOf(14, r + 2)
                else -> minOf(14, r + 3)
            }
            v <= 14 -> when {
                rand < 0.3f -> r
                rand < 0.6f -> minOf(14, r + 1)
                rand < 0.8f -> minOf(14, r + 2)
                else -> minOf(14, r + 3)
            }
            else -> {
                val g = minOf(24, 5 + (v - 15) / 2)
                when {
                    rand < 0.3f -> g
                    rand < 0.6f -> minOf(24, g + 1)
                    rand < 0.85f -> minOf(24, g + 2)
                    else -> minOf(24, g + 3)
                }
            }
        }
    }

    fun getFrozenPickaxeTier(level: Int, luckyLootLevel: Int): Int {
        val v = level + (luckyLootLevel - 1)
        val rand = Random.nextFloat()
        return when {
            v <= 2 -> 0
            v <= 5 -> if (rand < 0.9f) 0 else 1
            v <= 8 -> when {
                rand < 0.7f -> 0
                rand < 0.9f -> 1
                else -> 2
            }
            v <= 11 -> when {
                rand < 0.5f -> 0
                rand < 0.7f -> 1
                rand < 0.9f -> 2
                else -> 3
            }
            v <= 14 -> when {
                rand < 0.3f -> 0
                rand < 0.5f -> 1
                rand < 0.7f -> 2
                rand < 0.9f -> 3
                else -> 4
            }
            v <= 17 -> when {
                rand < 0.2f -> 4
                rand < 0.45f -> 5
                rand < 0.7f -> 6
                rand < 0.9f -> 7
                else -> 8
            }
            v <= 20 -> when {
                rand < 0.15f -> 8
                rand < 0.35f -> 9
                rand < 0.6f -> 10
                rand < 0.85f -> 11
                else -> 12
            }
            else -> when {
                rand < 0.1f -> 11
                rand < 0.3f -> 12
                else -> (8 + Random.nextInt(17)).coerceAtMost(24)
            }
        }
    }
}
