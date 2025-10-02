package org.gaseumlabs.uhcplugin.core.phase

import org.bukkit.GameRule
import org.gaseumlabs.uhcplugin.core.Game
import org.gaseumlabs.uhcplugin.core.UHCBorder
import org.gaseumlabs.uhcplugin.core.broadcast.Broadcast

class Shrink(val duration: Int) : Phase(PhaseType.SHRINK) {
	fun start(game: Game) {
		UHCBorder.shrink(game.gameWorld, game.finalRadius, game.shrinkPhase.duration)
		game.gameWorld.setGameRule(GameRule.NATURAL_REGENERATION, false)
		game.netherWorld.setGameRule(GameRule.NATURAL_REGENERATION, false)
		game.gameWorld.pvp = true
		Broadcast.broadcast(Broadcast.game("Border has started shrinking"))
	}
}