package org.gaseumlabs.uhcplugin.core.game

import org.bukkit.World
import org.gaseumlabs.uhcplugin.core.Summary

class PostGame(
	val summary: Summary,
	gameWorld: World,
	netherWorld: World,
) : Game(gameWorld, netherWorld) {
	override fun postTick() {}
}
