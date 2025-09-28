package org.gaseumlabs.uhcplugin.core

import org.bukkit.damage.DamageType
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.gaseumlabs.uhcplugin.core.playerData.PlayerData

class GameEvents : Listener {
	data class PlayerResult(val player: Player, val game: Game, val playerData: PlayerData)

	fun getPlayerData(entity: Entity): PlayerResult? {
		val player = entity as? Player ?: return null
		val game = UHC.getGame() ?: return null
		val playerData = game.playerDatas.get(player) ?: return null
		return PlayerResult(player, game, playerData)
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val (deathPlayer, game, playerData) = getPlayerData(event.player) ?: return

		event.isCancelled = true

		val deathLocation = deathPlayer.location

		val (killerUuid, deathMessage) = Death.getKiller(
			event.damageSource,
			deathPlayer,
			event.deathMessage()
		)

		UHC.onPlayerDeath(game, playerData, deathLocation, killerUuid, deathMessage, false)

		Death.dropPlayer(deathPlayer)
	}

	@EventHandler
	fun onDamage(event: EntityDamageEvent) {
		val (_, game) = getPlayerData(event.entity) ?: return

		if (game.isDone) {
			event.isCancelled = true
		} else if (!game.getPhase().type.pvp && event.damageSource.damageType === DamageType.LAVA) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onHealthChange(event: EntityRegainHealthEvent) {
		val (_, game) = getPlayerData(event.entity) ?: return

		if (game.isDone) {
			event.isCancelled = true
		}
	}
}