package org.gaseumlabs.uhcplugin.core

import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.gaseumlabs.uhcplugin.core.playerData.OfflineZombie
import org.gaseumlabs.uhcplugin.core.playerData.PlayerCapture
import org.gaseumlabs.uhcplugin.help.PlayerAdvancement
import org.gaseumlabs.uhcplugin.help.UHCAdvancements

class Login : Listener {
	@EventHandler
	fun onLogin(event: PlayerJoinEvent) {
		val player = event.player
		val game = UHC.activeGame() ?: return
		val playerData = game.playerDatas.get(player) ?: return

		PlayerAdvancement.grant(player, UHCAdvancements.UHC)

		val offlineRecord = playerData.offlineRecord

		val zombie = offlineRecord.zombie

		if (zombie != null) {
			PlayerManip.resetPlayer(player, zombie, playerData.maxHealth, offlineRecord.wipeMode)
		} else if (offlineRecord.spectateLocation != null) {
			PlayerManip.makeSpectator(player, offlineRecord.spectateLocation)
		}

		playerData.offlineRecord.onLogin()
	}

	@EventHandler
	fun onLeave(event: PlayerQuitEvent) {
		val player = event.player
		val game = UHC.activeGame() ?: return
		val playerData = game.playerDatas.get(player) ?: return

		if (!playerData.isActive) return

		when (player.gameMode) {
			GameMode.SURVIVAL,
			GameMode.ADVENTURE,
				-> {
				playerData.offlineRecord.onLogout(
					OfflineZombie.spawn(
						player.uniqueId,
						PlayerCapture.create(
							player.location,
							player.health,
							playerData.maxHealth,
							player.inventory,
							player.fireTicks,
							player.totalExperience,
							player.fallDistance,
							player.isSneaking || player.isSwimming
						)
					)
				)
			}

			GameMode.CREATIVE -> {}
			GameMode.SPECTATOR -> {
				playerData.offlineRecord.onDeath(player.location)
			}
		}
	}
}
