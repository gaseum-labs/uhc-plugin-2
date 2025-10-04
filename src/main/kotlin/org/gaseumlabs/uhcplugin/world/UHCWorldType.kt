package org.gaseumlabs.uhcplugin.world

import org.bukkit.World

enum class UHCWorldType(val worldName: String, val environment: World.Environment) {
	GAME("game", World.Environment.NORMAL),
	NETHER("game_nether", World.Environment.NETHER)
}
