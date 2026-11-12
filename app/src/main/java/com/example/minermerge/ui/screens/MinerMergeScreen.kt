package com.example.minermerge.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.minermerge.model.GamePhase
import com.example.minermerge.ui.components.BackgroundTerrain
import com.example.minermerge.ui.components.GameBottomBar
import com.example.minermerge.ui.components.GameTopBar
import com.example.minermerge.ui.components.MergeGridView
import com.example.minermerge.ui.components.MineShaftView
import com.example.minermerge.ui.dialogs.AdminPanelDialog
import com.example.minermerge.ui.dialogs.AllDestroyedDialog
import com.example.minermerge.ui.dialogs.LevelCompleteDialog
import com.example.minermerge.ui.dialogs.ResetConfirmDialog
import com.example.minermerge.ui.dialogs.SellConfirmDialog
import com.example.minermerge.ui.dialogs.UpgradeShopDialog
import com.example.minermerge.ui.theme.SkyCyan
import com.example.minermerge.viewmodel.GameViewModel

@Composable
fun MinerMergeScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_miner_merge"),
        containerColor = SkyCyan
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sky + parallax terrain backdrop, like the original.
            BackgroundTerrain(
                cameraScrollY = state.cameraScrollY,
                manualScrollY = state.manualScrollY,
                phase = state.phase,
                scrollMode = state.scrollMode,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top controls & Logo
                GameTopBar(
                    onResetClick = { viewModel.toggleResetConfirm(true) },
                    onUpgradesClick = { viewModel.toggleUpgradeShop(true) },
                    onSaveClick = {
                        viewModel.saveGame()
                        viewModel.soundManager.triggerHaptic("medium")
                    },
                    onAdminClick = { viewModel.toggleAdminPanel(true) },
                    currentLevel = state.currentLevel,
                    isAdminUnlocked = state.adminPanelUnlocked
                )

                // Merge Grid in SETUP phase
                if (state.phase == GamePhase.SETUP) {
                    MergeGridView(
                        pickaxes = state.pickaxes,
                        gifts = state.gifts,
                        selectedSlot = state.selectedGridSlot,
                        onSlotClick = { c, r -> viewModel.selectGridSlot(c, r) },
                        onGiftClick = { giftId -> viewModel.openGift(giftId) },
                        onMoveOrMerge = { fc, fr, tc, tr ->
                            viewModel.handleMoveOrMerge(fc, fr, tc, tr)
                        },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Mine Shaft View (Blocks & Falling physics)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    MineShaftView(
                        blocks = state.blocks,
                        fallingPickaxes = state.fallingPickaxes,
                        floatingCoins = state.floatingCoins,
                        cameraScrollY = state.cameraScrollY,
                        phase = state.phase,
                        scrollMode = state.scrollMode,
                        manualScrollY = state.manualScrollY,
                        onManualScroll = { viewModel.setManualScrollY(it) }
                    )
                }

                // Bottom Bar controls
                if (state.phase == GamePhase.SETUP) {
                    GameBottomBar(
                        coins = state.coins,
                        totalDrops = state.totalDrops,
                        onBuyClick = { viewModel.buyPickaxe() },
                        onDropClick = { viewModel.startDrop() },
                        isDropEnabled = state.pickaxes.isNotEmpty()
                    )
                }
            }

            // Dialog Overlays
            if (state.showUpgradeShop) {
                UpgradeShopDialog(
                    coins = state.coins,
                    luckyLootLevel = state.luckyLootLevel,
                    buyQualityLevel = state.buyQualityLevel,
                    luckyFreqLevel = state.luckyFreqLevel,
                    onUpgradeLuckyLoot = { viewModel.upgradeLuckyLoot() },
                    onUpgradeBuyQuality = { viewModel.upgradeBuyQuality() },
                    onUpgradeLuckyFreq = { viewModel.upgradeLuckyFreq() },
                    onDismiss = { viewModel.toggleUpgradeShop(false) }
                )
            }

            if (state.showAdminPanel) {
                AdminPanelDialog(
                    isUnlocked = state.adminPanelUnlocked,
                    codeInput = state.adminCodeInput,
                    onCodeChange = { viewModel.updateAdminCodeInput(it) },
                    onUnlock = { viewModel.unlockAdminPanel() },
                    onProgressLevel = { viewModel.adminProgressLevel() },
                    scrollMode = state.scrollMode,
                    onToggleScrollMode = { viewModel.toggleScrollMode() },
                    selectedTier = state.selectedDebugTier,
                    onTierChange = { viewModel.setDebugTier(it) },
                    onSpawnPickaxe = { viewModel.spawnDebugPickaxe() },
                    onDismiss = { viewModel.toggleAdminPanel(false) }
                )
            }

            if (state.showLevelCompleteDialog) {
                LevelCompleteDialog(
                    coinsEarned = state.levelCompleteReward?.coins ?: 50,
                    onNextLevel = { viewModel.proceedToNextLevel() }
                )
            }

            if (state.showAllDestroyedDialog) {
                AllDestroyedDialog(
                    coinsEarned = state.dropCoinsEarned,
                    onTryAgain = { viewModel.retryDrop() }
                )
            }

            state.sellConfirmPickaxe?.let { pickaxe ->
                SellConfirmDialog(
                    pickaxe = pickaxe,
                    onConfirm = { viewModel.confirmSell() },
                    onCancel = { viewModel.cancelSell() }
                )
            }

            if (state.showResetConfirm) {
                ResetConfirmDialog(
                    onConfirm = {
                        viewModel.toggleResetConfirm(false)
                        viewModel.resetGame()
                    },
                    onCancel = { viewModel.toggleResetConfirm(false) }
                )
            }
        }
    }
}
