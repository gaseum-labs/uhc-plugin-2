package org.gaseumlabs.uhcplugin.core.phase

import org.bukkit.GameRule
import org.gaseumlabs.uhcplugin.core.UHCBorder
import org.gaseumlabs.uhcplugin.core.broadcast.Broadcast
import org.gaseumlabs.uhcplugin.core.broadcast.MSG
import org.gaseumlabs.uhcplugin.core.game.ActiveGame

class Shrink(val duration: Int) : Phase(PhaseType.SHRINK) {
	fun start(activeGame: ActiveGame) {
		UHCBorder.shrink(activeGame.gameWorld, activeGame.finalRadius, activeGame.shrinkPhase.duration)
		activeGame.gameWorld.setGameRule(GameRule.NATURAL_REGENERATION, false)
		activeGame.netherWorld.setGameRule(GameRule.NATURAL_REGENERATION, false)
		activeGame.gameWorld.pvp = true
		Broadcast.broadcast(MSG.game("Border has started shrinking"))
	}
}