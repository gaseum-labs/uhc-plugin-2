package org.gaseumlabs.uhcplugin.help

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.gaseumlabs.uhcplugin.core.UHC
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import org.gaseumlabs.uhcplugin.core.playerData.PlayerData

class AdvancementEvents : Listener {
	private fun getPlayerData(player: Player): Pair<ActiveGame, PlayerData>? {
		val activeGame = UHC.activeGame() ?: return null
		return activeGame to (activeGame.playerDatas.get(player) ?: return null)
	}

	@EventHandler
	fun onAdvancement(event: PlayerAdvancementCriterionGrantEvent) {
		if (getPlayerData(event.player) == null) {
			event.isCancelled = true
			return
		}

		if (!UHCAdvancements.list.any { advancement ->
				advancement.key == event.advancement.key()
			}) {
			event.isCancelled = true
			return
		}
	}

	@EventHandler
	fun onTeleport(event: PlayerTeleportEvent) {
		val (game) = getPlayerData(event.player) ?: return

		if (event.to.world === game.netherWorld) {
			PlayerAdvancement.grant(event.player, UHCAdvancements.UHC_NETHER)
		}
	}
}
