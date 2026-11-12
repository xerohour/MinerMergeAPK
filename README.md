# Miner Merge (Android)

Miner Merge is a casual mining idle & puzzle game built natively for Android using Kotlin and Jetpack Compose.

## Features

- **Merge Grid**: 5x3 interactive grid to organize, merge, and upgrade pickaxes from Tier 1 up to Tier 25 (Diamond, Emerald, Obsidian, and beyond).
- **Sell Zone**: Dedicated sell slot to exchange duplicate or unwanted pickaxes for 50 coins.
- **Dynamic Mine Shaft**: Drop pickaxes with physics (gravity, bouncing, block damage, tool durability) into procedural mine levels.
- **Blocks & Ores**:
  - Grass, Dirt, Cobblestone, Stone
  - Coal, Iron, Gold, Redstone, Lapis, Diamond, Emerald
  - Ice blocks with trapped frozen pickaxes that join the drop when freed
  - TNT blocks triggering 3x3 explosive chain reactions
  - Lucky blocks containing mystery pickaxe gifts
  - Bonus gold blocks awarding 500 coins
  - Bottom chests to complete levels and earn victory coins + gift boxes
- **Upgrade Shop**:
  - **Lucky Loot**: Better pickaxe tiers from lucky gift boxes
  - **Buy Quality**: Higher starting pickaxe tiers when purchasing new pickaxes
  - **Lucky Frequency**: Increases the spawn rate of lucky blocks in deeper shafts
- **Admin Panel**: Debug tools protected by access code (`000`), allowing level advancement, free pickaxe tier spawning, and inspection.
- **Audio & Haptics**: Full background soundtrack playlist, synthesized retro sound effects for hits, chimes, explosions, and haptic vibrations.
- **Persistence**: Game state (coins, current level, inventory, upgrades, mined blocks) automatically saved to local storage.

## Tech Stack

- **Platform**: Android (minSdk 24, targetSdk 36)
- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose with Material Design 3
- **Build System**: Gradle 9.3.1 (Kotlin DSL) with Android Gradle Plugin 9.1.1
- **Architecture**: MVVM (Model-View-ViewModel) with Kotlin Coroutines & StateFlow
