package org.gaseumlabs.uhcplugin.core

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.World
import org.bukkit.damage.DamageType
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import org.gaseumlabs.uhcplugin.core.game.PostGame
import org.gaseumlabs.uhcplugin.core.playerData.PlayerData

class GameEvents : Listener {
	data class PlayerResult(val player: Player, val activeGame: ActiveGame, val playerData: PlayerData)

	fun getPlayerData(entity: Entity): PlayerResult? {
		val player = entity as? Player ?: return null
		val game = UHC.activeGame() ?: return null
		val playerData = game.playerDatas.get(player) ?: return null
		return PlayerResult(player, game, playerData)
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val (deathPlayer, game, playerData) = getPlayerData(event.player) ?: return

		val deathLocation = deathPlayer.location

		val (killerUuid, deathMessage) = Death.getKiller(
			event.damageSource,
			deathPlayer,
			event.deathMessage()
		)

		UHC.onPlayerDeath(game, playerData, deathLocation, killerUuid, deathMessage, false)

		Death.dropPlayer(deathPlayer)
		val respawnLocation = PlayerSpreader.getRespawnLocation(
			game,
			playerData,
			game.gameWorld,
			UHCBorder.getBorderRadius(game.gameWorld.worldBorder),
			100,
			PlayerSpreader.CONFIG_DEFAULT
		)
		deathPlayer.setRespawnLocation(respawnLocation, true)
	}
	@EventHandler
	fun onPlayerPortal(event: PlayerPortalEvent) {
		val (player, game, playerData) = getPlayerData(event.player) ?: return
		if (event.to.world.environment == World.Environment.NORMAL) return
		val block = player.location.block
		val team = playerData.team
		team.members.forEach { playerData ->
			playerData.getPlayer()?.sendMessage(
				Component.text("${player.name} entered the Nether at ", team.color.textColor).append(
					Component.text(
						"(${block.x}, ${block.y}, ${block.z})", team.color.textColor,
						TextDecoration.BOLD
					)
				)
			)
		}
	}

	@EventHandler
	fun onDamage(event: EntityDamageEvent) {
		val game = UHC.game() ?: return
		if (!game.isInWorld(event.entity)) return

		when (game) {
			is ActiveGame -> {
				game.playerDatas.getActive(event.entity as? Player ?: return) ?: return
				if (!game.getPhase().type.pvp && when (event.damageSource.damageType) {
						DamageType.LAVA,
						DamageType.ON_FIRE,
						DamageType.IN_FIRE,
							-> true

						else -> false
					}
				) {
					event.isCancelled = true
				}
			}

			is PostGame -> {
				if (event.entity is Player) {
					event.isCancelled = true
				}
			}
		}
	}

	@EventHandler
	fun onHealthChange(event: EntityRegainHealthEvent) {
		val game = UHC.postGame() ?: return

		if (event.entity is Player && game.isInWorld(event.entity)) {
			event.isCancelled = true
		}
	}
}