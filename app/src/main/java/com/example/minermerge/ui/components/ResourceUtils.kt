package com.example.minermerge.ui.components

import com.example.minermerge.R
import com.example.minermerge.model.BlockType

object ResourceUtils {

    fun getBlockDrawable(type: BlockType): Int {
        return when (type) {
            BlockType.GRASS -> R.drawable.block_grass
            BlockType.DIRT -> R.drawable.block_dirt
            BlockType.COBBLE -> R.drawable.block_cobble
            BlockType.STONE -> R.drawable.block_stone
            BlockType.COAL -> R.drawable.block_coal
            BlockType.IRON -> R.drawable.block_iron
            BlockType.GOLD -> R.drawable.block_gold
            BlockType.DIAMOND -> R.drawable.block_diamond
            BlockType.EMERALD -> R.drawable.block_emerald
            BlockType.REDSTONE -> R.drawable.block_redstone
            BlockType.LAPIS -> R.drawable.block_lapis
            BlockType.ICE -> R.drawable.block_ice
            BlockType.TNT -> R.drawable.block_tnt
            BlockType.LUCKY -> R.drawable.ic_gift_box
            BlockType.CHEST -> R.drawable.block_chest
        }
    }

    fun getCrackDrawable(stage: Int): Int? {
        return when (stage) {
            1 -> R.drawable.crack_0
            2 -> R.drawable.crack_1
            3 -> R.drawable.crack_2
            4 -> R.drawable.crack_3
            5 -> R.drawable.crack_4
            6 -> R.drawable.crack_5
            7 -> R.drawable.crack_6
            8 -> R.drawable.crack_7
            9 -> R.drawable.crack_8
            10 -> R.drawable.crack_9
            else -> null
        }
    }

    fun getPickaxeDrawable(tier: Int): Int {
        return when (tier.coerceIn(0, 24)) {
            0 -> R.drawable.pickaxe_0
            1 -> R.drawable.pickaxe_1
            2 -> R.drawable.pickaxe_2
            3 -> R.drawable.pickaxe_3
            4 -> R.drawable.pickaxe_4
            5 -> R.drawable.pickaxe_5
            6 -> R.drawable.pickaxe_6
            7 -> R.drawable.pickaxe_7
            8 -> R.drawable.pickaxe_8
            9 -> R.drawable.pickaxe_9
            10 -> R.drawable.pickaxe_10
            11 -> R.drawable.pickaxe_11
            12 -> R.drawable.pickaxe_12
            13 -> R.drawable.pickaxe_13
            14 -> R.drawable.pickaxe_14
            15 -> R.drawable.pickaxe_15
            16 -> R.drawable.pickaxe_16
            17 -> R.drawable.pickaxe_17
            18 -> R.drawable.pickaxe_18
            19 -> R.drawable.pickaxe_19
            20 -> R.drawable.pickaxe_20
            21 -> R.drawable.pickaxe_21
            22 -> R.drawable.pickaxe_22
            23 -> R.drawable.pickaxe_23
            24 -> R.drawable.pickaxe_24
            else -> R.drawable.pickaxe_0
        }
    }
}
