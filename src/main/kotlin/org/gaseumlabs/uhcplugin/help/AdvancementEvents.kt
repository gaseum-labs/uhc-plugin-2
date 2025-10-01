package org.gaseumlabs.uhcplugin.help

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.gaseumlabs.uhcplugin.core.Game
import org.gaseumlabs.uhcplugin.core.UHC
import org.gaseumlabs.uhcplugin.core.playerData.PlayerData

class AdvancementEvents : Listener {
	private fun getPlayerData(player: Player): Pair<Game, PlayerData>? {
		val game = UHC.getGame() ?: return null
		return game to (game.playerDatas.get(player) ?: return null)
	}

	@EventHandler
	fun onAdvancement(event: PlayerAdvancementCriterionGrantEvent) {
		if (getPlayerData(event.player) == null) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onTeleport(event: PlayerTeleportEvent) {
		val (game) = getPlayerData(event.player) ?: run {
			event.isCancelled = true
			return
		}

		if (event.to.world === game.netherWorld) {
			PlayerAdvancement.grant(event.player, UHCAdvancements.UHC_NETHER)
		}
	}
}
