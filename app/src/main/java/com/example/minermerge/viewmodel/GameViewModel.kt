package com.example.minermerge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.minermerge.audio.SoundManager
import com.example.minermerge.data.GameRepository
import com.example.minermerge.data.SavedGameState
import com.example.minermerge.model.BlockItem
import com.example.minermerge.model.BlockType
import com.example.minermerge.model.FloatingCoin
import com.example.minermerge.model.GameFormulas
import com.example.minermerge.model.GameParticle
import com.example.minermerge.model.GamePhase
import com.example.minermerge.model.GiftItem
import com.example.minermerge.model.PickaxeItem
import com.example.minermerge.model.PickaxeTiers
import com.example.minermerge.model.calculateBlockCoins
import com.example.minermerge.model.calculateBlockHealth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class GameUiState(
    val coins: Int = 50,
    val currentLevel: Int = 1,
    val phase: GamePhase = GamePhase.SETUP,
    val pickaxes: List<PickaxeItem> = emptyList(),
    val fallingPickaxes: List<PickaxeItem> = emptyList(),
    val gifts: List<GiftItem> = emptyList(),
    val blocks: List<BlockItem> = emptyList(),
    val floatingCoins: List<FloatingCoin> = emptyList(),
    val particles: List<GameParticle> = emptyList(),
    val cameraScrollY: Float = 0f,
    val shakeIntensity: Float = 0f,
    val luckyLootLevel: Int = 1,
    val buyQualityLevel: Int = 1,
    val luckyFreqLevel: Int = 1,
    val totalDrops: Int = 0,
    val dropCoinsEarned: Int = 0,
    val chestsReached: Int = 0,
    val levelCompleteReward: LevelReward? = null,
    val showAllDestroyedDialog: Boolean = false,
    val showLevelCompleteDialog: Boolean = false,
    val showUpgradeShop: Boolean = false,
    val showAdminPanel: Boolean = false,
    val showResetConfirm: Boolean = false,
    val sellConfirmPickaxe: PickaxeItem? = null,
    val adminPanelUnlocked: Boolean = false,
    val adminCodeInput: String = "",
    val scrollMode: Boolean = false,
    val manualScrollY: Float = 0f,
    val selectedDebugTier: Int = 0,
    val selectedGridSlot: Pair<Int, Int>? = null
)

data class LevelReward(
    val coins: Int,
    val gift: GiftItem
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)
    val soundManager = SoundManager(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val usedGiftIds = mutableSetOf<String>()
    private val usedIceBlockIds = mutableSetOf<String>()
    private val usedTntBlockIds = mutableSetOf<String>()
    private val destroyedBlockIds = mutableSetOf<String>()

    private var dropLoopJob: Job? = null
    private var snapshotPickaxes: List<PickaxeItem> = emptyList()
    private var snapshotBlocks: List<BlockItem> = emptyList()
    private var snapshotGifts: List<GiftItem> = emptyList()

    init {
        loadGame()
    }

    private fun loadGame() {
        val saved = repository.loadGameState()
        usedGiftIds.addAll(saved.usedGiftIds)
        usedIceBlockIds.addAll(saved.usedIceBlockIds)
        usedTntBlockIds.addAll(saved.usedTntBlockIds)
        destroyedBlockIds.addAll(saved.destroyedBlockIds)

        var loadedPickaxes = saved.pickaxes
        if (loadedPickaxes.isEmpty()) {
            val stats = PickaxeTiers.getStats(0)
            loadedPickaxes = listOf(
                PickaxeItem(
                    id = "pickaxe-init-${System.currentTimeMillis()}",
                    type = 0,
                    health = stats.health,
                    maxHealth = stats.health,
                    gridCol = 2,
                    gridRow = 1
                )
            )
        }

        _uiState.update {
            it.copy(
                coins = saved.coins,
                currentLevel = saved.currentLevel,
                pickaxes = loadedPickaxes,
                gifts = saved.collectedGifts,
                luckyLootLevel = saved.luckyLootLevel,
                buyQualityLevel = saved.buyQualityLevel,
                luckyFreqLevel = saved.luckyFreqLevel,
                totalDrops = saved.totalDrops,
                adminPanelUnlocked = saved.adminPanelUnlocked
            )
        }

        generateLevel(saved.currentLevel)
    }

    fun saveGame() {
        val state = _uiState.value
        val toSave = SavedGameState(
            coins = state.coins,
            currentLevel = state.currentLevel,
            pickaxes = state.pickaxes,
            collectedGifts = state.gifts,
            usedGiftIds = usedGiftIds.toList(),
            usedIceBlockIds = usedIceBlockIds.toList(),
            usedTntBlockIds = usedTntBlockIds.toList(),
            destroyedBlockIds = destroyedBlockIds.toList(),
            luckyLootLevel = state.luckyLootLevel,
            buyQualityLevel = state.buyQualityLevel,
            luckyFreqLevel = state.luckyFreqLevel,
            totalDrops = state.totalDrops,
            adminPanelUnlocked = state.adminPanelUnlocked
        )
        repository.saveGameState(toSave)
    }

    fun generateLevel(level: Int) {
        val totalRows = 7 + level - 1
        val innerRows = (1 until totalRows - 1).toList()
        val allInnerCells = mutableListOf<Pair<Int, Int>>()
        for (r in innerRows) {
            for (c in 0 until 5) {
                allInnerCells.add(r to c)
            }
        }
        allInnerCells.shuffle()

        val luckyCount = min(_uiState.value.luckyFreqLevel, allInnerCells.size)
        val luckyCells = allInnerCells.take(luckyCount).toSet()

        val tntMax = 2
        val tntCount = Random.nextInt(0, tntMax + 1)
        val tntCells = allInnerCells.drop(luckyCount).take(tntCount).toSet()

        val bonusCell = allInnerCells.drop(luckyCount + tntCount).firstOrNull()

        val newBlocks = mutableListOf<BlockItem>()

        for (r in 0 until totalRows) {
            for (c in 0 until 5) {
                val cellKey = r to c
                val blockId = "block-$r-$c"

                if (r == 0) {
                    val hp = calculateBlockHealth(BlockType.GRASS, level)
                    newBlocks.add(
                        BlockItem(
                            id = blockId,
                            col = c,
                            row = r,
                            type = BlockType.GRASS,
                            health = hp,
                            maxHealth = hp,
                            isDigged = destroyedBlockIds.contains(blockId)
                        )
                    )
                } else if (r == totalRows - 1) {
                    val hp = calculateBlockHealth(BlockType.CHEST, level)
                    newBlocks.add(
                        BlockItem(
                            id = blockId,
                            col = c,
                            row = r,
                            type = BlockType.CHEST,
                            health = hp,
                            maxHealth = hp,
                            isDigged = false
                        )
                    )
                } else if (cellKey == bonusCell) {
                    val hp = calculateBlockHealth(BlockType.DIRT, level)
                    newBlocks.add(
                        BlockItem(
                            id = blockId,
                            col = c,
                            row = r,
                            type = BlockType.DIRT,
                            health = hp,
                            maxHealth = hp,
                            isBonus = true,
                            isDigged = destroyedBlockIds.contains(blockId)
                        )
                    )
                } else if (luckyCells.contains(cellKey)) {
                    val giftId = "gift-$r-$c"
                    if (!usedGiftIds.contains(giftId) && !destroyedBlockIds.contains(giftId)) {
                        newBlocks.add(
                            BlockItem(
                                id = giftId,
                                col = c,
                                row = r,
                                type = BlockType.LUCKY,
                                health = 1,
                                maxHealth = 1,
                                giftId = giftId,
                                isDigged = false
                            )
                        )
                    }
                } else if (tntCells.contains(cellKey)) {
                    val tntId = "tnt-$r-$c"
                    if (!usedTntBlockIds.contains(tntId) && !destroyedBlockIds.contains(tntId)) {
                        newBlocks.add(
                            BlockItem(
                                id = tntId,
                                col = c,
                                row = r,
                                type = BlockType.TNT,
                                health = 1,
                                maxHealth = 1,
                                isDigged = false
                            )
                        )
                    }
                } else {
                    if (destroyedBlockIds.contains(blockId)) continue

                    val rand = Random.nextFloat()
                    val totalDrops = _uiState.value.totalDrops
                    val diamondProb = if (totalDrops >= 15) min(0.25f, 0.05f + (totalDrops - 15) * 0.015f) else 0f
                    val emeraldProb = if (totalDrops >= 25) min(0.20f, 0.03f + (totalDrops - 25) * 0.01f) else 0f
                    val cobbleProb = when (level) {
                        1 -> 0f
                        2 -> 0.05f
                        else -> 0.20f
                    }

                    var bType: BlockType = when {
                        rand < emeraldProb -> BlockType.EMERALD
                        rand < diamondProb -> BlockType.DIAMOND
                        level >= 21 && rand < 0.08f -> BlockType.DIAMOND
                        level >= 18 && rand < 0.04f -> BlockType.LAPIS
                        level >= 15 && rand < 0.05f -> BlockType.REDSTONE
                        level >= 9 && rand < 0.07f -> BlockType.GOLD
                        level >= 6 && rand < 0.08f -> BlockType.IRON
                        level >= 3 && rand < 0.10f -> BlockType.COAL
                        rand < cobbleProb -> BlockType.COBBLE
                        else -> BlockType.DIRT
                    }

                    var frozenTier: Int? = null
                    if (level >= 5 && bType == BlockType.DIRT && r >= 4 && r < totalRows - 2 && Random.nextFloat() < 0.04f) {
                        bType = BlockType.ICE
                        frozenTier = GameFormulas.getFrozenPickaxeTier(level, _uiState.value.luckyLootLevel)
                    }

                    val hp = calculateBlockHealth(bType, level)
                    newBlocks.add(
                        BlockItem(
                            id = blockId,
                            col = c,
                            row = r,
                            type = bType,
                            health = hp,
                            maxHealth = hp,
                            frozenPickaxeTier = frozenTier,
                            isDigged = false
                        )
                    )
                }
            }
        }

        _uiState.update {
            it.copy(
                blocks = newBlocks,
                phase = GamePhase.SETUP,
                fallingPickaxes = emptyList(),
                floatingCoins = emptyList(),
                particles = emptyList(),
                cameraScrollY = 0f,
                manualScrollY = 0f,
                dropCoinsEarned = 0,
                chestsReached = 0,
                levelCompleteReward = null,
                showAllDestroyedDialog = false,
                showLevelCompleteDialog = false
            )
        }
    }

    fun buyPickaxe() {
        val state = _uiState.value
        if (state.coins < 50) return
        val occupied = state.pickaxes.mapNotNull { p ->
            if (p.gridCol != null && p.gridRow != null) p.gridCol!! to p.gridRow!! else null
        }.toSet()

        if (occupied.size >= 15) return

        // Find first free slot
        var targetSlot: Pair<Int, Int>? = null
        for (r in 0 until 3) {
            for (c in 0 until 5) {
                if (!occupied.contains(c to r)) {
                    targetSlot = c to r
                    break
                }
            }
            if (targetSlot != null) break
        }

        val slot = targetSlot ?: return
        val tier = (state.buyQualityLevel - 1).coerceIn(0, 24)
        val stats = PickaxeTiers.getStats(tier)

        val newPickaxe = PickaxeItem(
            id = "pickaxe-${System.currentTimeMillis()}-${Random.nextInt(1000)}",
            type = tier,
            health = stats.health,
            maxHealth = stats.health,
            gridCol = slot.first,
            gridRow = slot.second
        )

        _uiState.update {
            it.copy(
                coins = it.coins - 50,
                pickaxes = it.pickaxes + newPickaxe
            )
        }
        soundManager.playHitSound()
        soundManager.triggerHaptic("light")
        saveGame()
    }

    fun selectGridSlot(col: Int, row: Int) {
        val current = _uiState.value.selectedGridSlot
        if (current == null) {
            // First tap: select if there is an item at col, row
            val hasItem = _uiState.value.pickaxes.any { it.gridCol == col && it.gridRow == row } ||
                    _uiState.value.gifts.any { it.gridCol == col && it.gridRow == row }
            if (hasItem) {
                _uiState.update { it.copy(selectedGridSlot = col to row) }
                soundManager.triggerHaptic("light")
            }
        } else {
            // Second tap: move or merge or sell!
            if (col == 5) {
                // Sell column!
                val pickaxeToSell = _uiState.value.pickaxes.find { it.gridCol == current.first && it.gridRow == current.second }
                if (pickaxeToSell != null) {
                    _uiState.update { it.copy(selectedGridSlot = null, sellConfirmPickaxe = pickaxeToSell) }
                } else {
                    _uiState.update { it.copy(selectedGridSlot = null) }
                }
                return
            }

            handleMoveOrMerge(current.first, current.second, col, row)
            _uiState.update { it.copy(selectedGridSlot = null) }
        }
    }

    fun handleMoveOrMerge(fromCol: Int, fromRow: Int, toCol: Int, toRow: Int) {
        if (fromCol == toCol && fromRow == toRow) return

        if (toCol == 5) {
            val item = _uiState.value.pickaxes.find { it.gridCol == fromCol && it.gridRow == fromRow }
            if (item != null) {
                _uiState.update { it.copy(sellConfirmPickaxe = item) }
            }
            return
        }

        if (toCol !in 0..4 || toRow !in 0..2) return

        val sourcePickaxe = _uiState.value.pickaxes.find { it.gridCol == fromCol && it.gridRow == fromRow }
        val targetPickaxe = _uiState.value.pickaxes.find { it.gridCol == toCol && it.gridRow == toRow }

        if (sourcePickaxe != null && targetPickaxe != null) {
            if (sourcePickaxe.type == targetPickaxe.type && sourcePickaxe.type < 24) {
                // Merge into next tier!
                val nextTier = sourcePickaxe.type + 1
                val stats = PickaxeTiers.getStats(nextTier)
                val merged = targetPickaxe.copy(
                    type = nextTier,
                    health = stats.health,
                    maxHealth = stats.health
                )
                _uiState.update { s ->
                    s.copy(
                        pickaxes = s.pickaxes.filter { it.id != sourcePickaxe.id && it.id != targetPickaxe.id } + merged
                    )
                }
                soundManager.playMergeSound()
                soundManager.triggerHaptic("heavy")
                saveGame()
                return
            } else {
                // Swap positions
                _uiState.update { s ->
                    s.copy(
                        pickaxes = s.pickaxes.map {
                            when (it.id) {
                                sourcePickaxe.id -> it.copy(gridCol = toCol, gridRow = toRow)
                                targetPickaxe.id -> it.copy(gridCol = fromCol, gridRow = fromRow)
                                else -> it
                            }
                        }
                    )
                }
                soundManager.triggerHaptic("light")
                saveGame()
                return
            }
        }

        if (sourcePickaxe != null && targetPickaxe == null) {
            // Move pickaxe
            _uiState.update { s ->
                s.copy(
                    pickaxes = s.pickaxes.map {
                        if (it.id == sourcePickaxe.id) it.copy(gridCol = toCol, gridRow = toRow) else it
                    }
                )
            }
            soundManager.triggerHaptic("light")
            saveGame()
        }
    }

    fun confirmSell() {
        val pickaxe = _uiState.value.sellConfirmPickaxe ?: return
        _uiState.update { s ->
            s.copy(
                coins = s.coins + 50,
                pickaxes = s.pickaxes.filter { it.id != pickaxe.id },
                sellConfirmPickaxe = null
            )
        }
        soundManager.playGiftSound()
        soundManager.triggerHaptic("medium")
        saveGame()
    }

    fun cancelSell() {
        _uiState.update { it.copy(sellConfirmPickaxe = null) }
    }

    fun openGift(giftId: String) {
        val state = _uiState.value
        val gift = state.gifts.find { it.id == giftId } ?: return

        usedGiftIds.add(giftId)
        val occupied = state.pickaxes.mapNotNull { p ->
            if (p.gridCol != null && p.gridRow != null) p.gridCol!! to p.gridRow!! else null
        }.toSet()

        var targetSlot: Pair<Int, Int>? = null
        for (r in 0 until 3) {
            for (c in 0 until 5) {
                if (!occupied.contains(c to r)) {
                    targetSlot = c to r
                    break
                }
            }
            if (targetSlot != null) break
        }

        val slot = targetSlot ?: (0 to 0)
        val tier = GameFormulas.getGiftPickaxeTier(state.currentLevel, state.luckyLootLevel)
        val stats = PickaxeTiers.getStats(tier)
        val spawned = PickaxeItem(
            id = "gift-pickaxe-${System.currentTimeMillis()}-${Random.nextInt(1000)}",
            type = tier,
            health = stats.health,
            maxHealth = stats.health,
            gridCol = slot.first,
            gridRow = slot.second
        )

        _uiState.update { s ->
            s.copy(
                gifts = s.gifts.filter { it.id != giftId },
                pickaxes = s.pickaxes + spawned
            )
        }
        soundManager.playGiftSound()
        soundManager.triggerHaptic("medium")
        saveGame()
    }

    fun startDrop() {
        val state = _uiState.value
        if (state.phase == GamePhase.DROPPING) return

        snapshotPickaxes = state.pickaxes.map { it.copy() }
        snapshotBlocks = state.blocks.map { it.copy() }
        snapshotGifts = state.gifts.map { it.copy() }

        val falling = state.pickaxes.map { p ->
            p.copy(
                x = (p.gridCol ?: 0) * 64f + 32f,
                y = 0f,
                vx = (Random.nextFloat() - 0.5f) * 2f,
                vy = 2f
            )
        }

        _uiState.update {
            it.copy(
                phase = GamePhase.DROPPING,
                fallingPickaxes = falling,
                dropCoinsEarned = 0,
                chestsReached = 0,
                manualScrollY = 0f,
                totalDrops = it.totalDrops + 1,
                floatingCoins = emptyList(),
                particles = emptyList()
            )
        }

        soundManager.triggerHaptic("medium")
        runDropLoop()
    }

    private fun runDropLoop() {
        dropLoopJob?.cancel()
        dropLoopJob = viewModelScope.launch {
            val blockSize = 64f
            val topShaftY = 120f // start of blocks
            var tickCount = 0

            while (isActive) {
                delay(16) // ~60 fps
                tickCount++

                val currentState = _uiState.value
                val falling = currentState.fallingPickaxes.map { it.copy() }.toMutableList()
                val currentBlocks = currentState.blocks.map { it.copy() }.toMutableList()
                val newCoins = mutableListOf<FloatingCoin>()
                val newParticles = mutableListOf<GameParticle>()
                val freedPickaxes = mutableListOf<PickaxeItem>()

                var earnedThisTick = 0
                var chestsHitThisTick = 0

                val iterator = falling.iterator()
                while (iterator.hasNext()) {
                    val pickaxe = iterator.next()
                    pickaxe.vy += 0.55f // Gravity
                    pickaxe.y += pickaxe.vy
                    pickaxe.x += pickaxe.vx
                    pickaxe.vx *= 0.98f
                    pickaxe.rotation = (pickaxe.rotation + 8f) % 360f

                    // Keep inside shafts horizontally (5 columns * 64 = 320)
                    if (pickaxe.x < 16f) {
                        pickaxe.x = 16f
                        pickaxe.vx = -pickaxe.vx * 0.5f
                    } else if (pickaxe.x > 320f - 16f) {
                        pickaxe.x = 320f - 16f
                        pickaxe.vx = -pickaxe.vx * 0.5f
                    }

                    // Check collision with blocks
                    for (block in currentBlocks) {
                        if (block.isDigged) continue

                        val blockX = block.col * blockSize
                        val blockY = topShaftY + block.row * blockSize

                        if (pickaxe.x + 20f > blockX && pickaxe.x - 20f < blockX + blockSize &&
                            pickaxe.y + 20f > blockY && pickaxe.y - 20f < blockY + blockSize
                        ) {
                            // Hit!
                            when (block.type) {
                                BlockType.CHEST -> {
                                    if (!block.hasPickaxe) {
                                        block.hasPickaxe = true
                                        block.isAnimating = true
                                        chestsHitThisTick++
                                        soundManager.playChestSound()
                                        soundManager.triggerHaptic("heavy")
                                    }
                                    pickaxe.health = 0
                                }
                                BlockType.TNT -> {
                                    block.isDigged = true
                                    block.health = 0
                                    usedTntBlockIds.add(block.id)
                                    soundManager.playExplosionSound()
                                    soundManager.triggerHaptic("heavy")

                                    // Explode surrounding 3x3 blocks
                                    for (hb in currentBlocks) {
                                        if (hb.isDigged || hb.type == BlockType.CHEST) continue
                                        val dc = hb.col - block.col
                                        val dr = hb.row - block.row
                                        if (dc in -1..1 && dr in -1..1) {
                                            hb.isDigged = true
                                            hb.health = 0
                                            destroyedBlockIds.add(hb.id)
                                            val cVal = calculateBlockCoins(hb.type, currentState.currentLevel)
                                            if (cVal > 0) {
                                                earnedThisTick += cVal
                                                newCoins.add(FloatingCoin(
                                                    id = "coin-${System.currentTimeMillis()}-${Random.nextInt(10000)}",
                                                    x = hb.col * blockSize + 32f,
                                                    y = topShaftY + hb.row * blockSize + 32f,
                                                    amount = cVal
                                                ))
                                            }
                                        }
                                    }
                                    pickaxe.vy = -7f
                                    pickaxe.y = blockY - 20f
                                }
                                BlockType.LUCKY -> {
                                    block.isDigged = true
                                    block.health = 0
                                    val gId = block.giftId ?: block.id
                                    usedGiftIds.add(gId)
                                    val newGift = GiftItem(id = gId)
                                    _uiState.update { s -> s.copy(gifts = s.gifts + newGift) }
                                    soundManager.playGiftSound()
                                    soundManager.triggerHaptic("medium")
                                    pickaxe.vy = -7f
                                    pickaxe.y = blockY - 20f
                                }
                                BlockType.ICE -> {
                                    val dmg = PickaxeTiers.getStats(pickaxe.type).damage
                                    block.health -= dmg
                                    block.digStage = min(9, ((block.maxHealth - block.health).toFloat() / block.maxHealth * 10).toInt())
                                    if (block.health <= 0) {
                                        block.isDigged = true
                                        usedIceBlockIds.add(block.id)
                                        val freedTier = block.frozenPickaxeTier ?: 0
                                        val fStats = PickaxeTiers.getStats(freedTier)
                                        freedPickaxes.add(
                                            PickaxeItem(
                                                id = "freed-${System.currentTimeMillis()}-${Random.nextInt(10000)}",
                                                type = freedTier,
                                                health = fStats.health,
                                                maxHealth = fStats.health,
                                                x = blockX + 32f,
                                                y = blockY + 32f,
                                                vx = (Random.nextFloat() - 0.5f) * 4f,
                                                vy = -3f
                                            )
                                        )
                                        soundManager.playGiftSound()
                                    }
                                    pickaxe.health -= 1
                                    pickaxe.vy = -7f
                                    pickaxe.y = blockY - 20f
                                    soundManager.playHitSound()
                                    soundManager.triggerHaptic("light")
                                }
                                else -> {
                                    // Normal block / bonus
                                    if (block.isBonus) {
                                        block.isDigged = true
                                        block.health = 0
                                        destroyedBlockIds.add(block.id)
                                        earnedThisTick += 500
                                        newCoins.add(FloatingCoin(
                                            id = "bonus-${System.currentTimeMillis()}",
                                            x = blockX + 32f,
                                            y = blockY + 32f,
                                            amount = 500
                                        ))
                                        soundManager.playChestSound()
                                        soundManager.triggerHaptic("heavy")
                                    } else {
                                        val dmg = PickaxeTiers.getStats(pickaxe.type).damage
                                        block.health -= dmg
                                        block.digStage = min(9, ((block.maxHealth - block.health).toFloat() / block.maxHealth * 10).toInt())
                                        if (block.health <= 0) {
                                            block.isDigged = true
                                            destroyedBlockIds.add(block.id)
                                            val cVal = calculateBlockCoins(block.type, currentState.currentLevel)
                                            if (cVal > 0) {
                                                earnedThisTick += cVal
                                                newCoins.add(FloatingCoin(
                                                    id = "coin-${System.currentTimeMillis()}-${Random.nextInt(10000)}",
                                                    x = blockX + 32f,
                                                    y = blockY + 32f,
                                                    amount = cVal
                                                ))
                                            }
                                        }
                                        soundManager.playHitSound()
                                        soundManager.triggerHaptic("light")
                                    }
                                    pickaxe.health -= 1
                                    pickaxe.vy = -7f
                                    pickaxe.y = blockY - 20f
                                }
                            }
                            break
                        }
                    }

                    if (pickaxe.health <= 0) {
                        iterator.remove()
                    }
                }

                falling.addAll(freedPickaxes)

                // Update particles and floating numbers
                val updatedCoins = (currentState.floatingCoins + newCoins).mapNotNull {
                    if (it.age > 45) null else it.copy(y = it.y - 1.2f, age = it.age + 1)
                }

                // Camera follow: smoothly track deepest pickaxe
                val deepestPickaxeY = falling.maxOfOrNull { it.y } ?: currentState.cameraScrollY
                val targetScrollY = max(0f, deepestPickaxeY - 250f)
                val newScrollY = currentState.cameraScrollY + (targetScrollY - currentState.cameraScrollY) * 0.1f

                _uiState.update { s ->
                    s.copy(
                        fallingPickaxes = falling,
                        blocks = currentBlocks,
                        coins = s.coins + earnedThisTick,
                        dropCoinsEarned = s.dropCoinsEarned + earnedThisTick,
                        chestsReached = s.chestsReached + chestsHitThisTick,
                        floatingCoins = updatedCoins,
                        cameraScrollY = newScrollY
                    )
                }

                // Check termination conditions
                if (falling.isEmpty()) {
                    delay(300)
                    val finalState = _uiState.value
                    if (finalState.chestsReached > 0) {
                        val rewardCoins = 50 * finalState.chestsReached
                        val rewardGift = GiftItem(id = "reward-${System.currentTimeMillis()}")
                        _uiState.update { s ->
                            s.copy(
                                coins = s.coins + rewardCoins,
                                levelCompleteReward = LevelReward(rewardCoins, rewardGift),
                                showLevelCompleteDialog = true
                            )
                        }
                    } else {
                        _uiState.update { s ->
                            s.copy(showAllDestroyedDialog = true)
                        }
                    }
                    saveGame()
                    break
                }
            }
        }
    }

    fun proceedToNextLevel() {
        val state = _uiState.value
        val reward = state.levelCompleteReward
        val newGifts = if (reward != null) state.gifts + reward.gift else state.gifts
        val nextLevel = state.currentLevel + 1

        usedGiftIds.clear()
        usedIceBlockIds.clear()
        usedTntBlockIds.clear()
        destroyedBlockIds.clear()

        _uiState.update {
            it.copy(
                currentLevel = nextLevel,
                gifts = newGifts,
                showLevelCompleteDialog = false,
                levelCompleteReward = null
            )
        }
        generateLevel(nextLevel)
        saveGame()
    }

    fun retryDrop() {
        // Return to setup, keeping mined blocks
        val restoredPickaxes = snapshotPickaxes.map { it.copy() }
        _uiState.update {
            it.copy(
                phase = GamePhase.SETUP,
                pickaxes = restoredPickaxes,
                fallingPickaxes = emptyList(),
                showAllDestroyedDialog = false,
                cameraScrollY = 0f
            )
        }
        saveGame()
    }

    fun upgradeLuckyLoot() {
        val state = _uiState.value
        val cost = GameFormulas.upgradeCost(state.luckyLootLevel)
        if (state.coins >= cost) {
            _uiState.update {
                it.copy(
                    coins = it.coins - cost,
                    luckyLootLevel = it.luckyLootLevel + 1
                )
            }
            soundManager.playGiftSound()
            soundManager.triggerHaptic("heavy")
            saveGame()
        }
    }

    fun upgradeBuyQuality() {
        val state = _uiState.value
        val cost = GameFormulas.upgradeCost(state.buyQualityLevel)
        if (state.coins >= cost && state.buyQualityLevel < 25) {
            _uiState.update {
                it.copy(
                    coins = it.coins - cost,
                    buyQualityLevel = it.buyQualityLevel + 1
                )
            }
            soundManager.playGiftSound()
            soundManager.triggerHaptic("heavy")
            saveGame()
        }
    }

    fun upgradeLuckyFreq() {
        val state = _uiState.value
        val cost = GameFormulas.upgradeCost(state.luckyFreqLevel)
        if (state.coins >= cost) {
            _uiState.update {
                it.copy(
                    coins = it.coins - cost,
                    luckyFreqLevel = it.luckyFreqLevel + 1
                )
            }
            soundManager.playGiftSound()
            soundManager.triggerHaptic("heavy")
            saveGame()
        }
    }

    fun toggleUpgradeShop(show: Boolean) {
        _uiState.update { it.copy(showUpgradeShop = show) }
    }

    fun toggleAdminPanel(show: Boolean) {
        _uiState.update { it.copy(showAdminPanel = show) }
    }

    fun toggleResetConfirm(show: Boolean) {
        _uiState.update { it.copy(showResetConfirm = show) }
    }

    fun updateAdminCodeInput(code: String) {
        _uiState.update { it.copy(adminCodeInput = code) }
    }

    fun unlockAdminPanel() {
        if (_uiState.value.adminCodeInput == "000") {
            _uiState.update { it.copy(adminPanelUnlocked = true, adminCodeInput = "") }
            saveGame()
        }
    }

    fun adminProgressLevel() {
        val nextLevel = _uiState.value.currentLevel + 1
        _uiState.update { it.copy(currentLevel = nextLevel) }
        generateLevel(nextLevel)
        saveGame()
    }

    fun toggleScrollMode() {
        _uiState.update { it.copy(scrollMode = !it.scrollMode) }
    }

    fun setManualScrollY(value: Float) {
        _uiState.update { it.copy(manualScrollY = value) }
    }

    fun setDebugTier(tier: Int) {
        _uiState.update { it.copy(selectedDebugTier = tier.coerceIn(0, 24)) }
    }

    fun spawnDebugPickaxe() {
        val state = _uiState.value
        val occupied = state.pickaxes.mapNotNull { p ->
            if (p.gridCol != null && p.gridRow != null) p.gridCol!! to p.gridRow!! else null
        }.toSet()

        for (r in 0 until 3) {
            for (c in 0 until 5) {
                if (!occupied.contains(c to r)) {
                    val stats = PickaxeTiers.getStats(state.selectedDebugTier)
                    val spawned = PickaxeItem(
                        id = "debug-pickaxe-${System.currentTimeMillis()}",
                        type = state.selectedDebugTier,
                        health = stats.health,
                        maxHealth = stats.health,
                        gridCol = c,
                        gridRow = r
                    )
                    _uiState.update { it.copy(pickaxes = it.pickaxes + spawned) }
                    soundManager.playGiftSound()
                    saveGame()
                    return
                }
            }
        }
    }

    fun resetGame() {
        repository.clearAllData()
        usedGiftIds.clear()
        usedIceBlockIds.clear()
        usedTntBlockIds.clear()
        destroyedBlockIds.clear()

        val stats = PickaxeTiers.getStats(0)
        val initialPickaxe = PickaxeItem(
            id = "pickaxe-0",
            type = 0,
            health = stats.health,
            maxHealth = stats.health,
            gridCol = 2,
            gridRow = 1
        )

        _uiState.value = GameUiState(
            coins = 50,
            currentLevel = 1,
            pickaxes = listOf(initialPickaxe)
        )
        generateLevel(1)
        saveGame()
    }

    override fun onCleared() {
        super.onCleared()
        dropLoopJob?.cancel()
        soundManager.release()
    }
}
