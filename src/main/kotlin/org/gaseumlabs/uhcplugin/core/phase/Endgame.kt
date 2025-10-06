package org.gaseumlabs.uhcplugin.core.phase

import org.bukkit.Axis
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.gaseumlabs.uhcplugin.core.PlayerManip
import org.gaseumlabs.uhcplugin.core.UHCBorder
import org.gaseumlabs.uhcplugin.core.broadcast.Broadcast
import org.gaseumlabs.uhcplugin.core.broadcast.MSG
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import org.gaseumlabs.uhcplugin.core.timer.MultiTimerHolder
import org.gaseumlabs.uhcplugin.core.timer.TickTime
import org.gaseumlabs.uhcplugin.core.timer.YDamageTimer
import org.gaseumlabs.uhcplugin.fix.PortalFix
import org.gaseumlabs.uhcplugin.util.MathUtil

class EndgamePhase(val collapseTime: Int, val finalYRange: IntRange) : Phase(PhaseType.ENDGAME) {
	var warningTimers = MultiTimerHolder<YDamageTimer>()
	var glowingTimer: Int = 0
	val verticalBorder = VerticalBorder()

	fun getAlongYRange(along: Double): IntRange {
		val low = MathUtil.lerp(VerticalBorder.WORLD_Y_RANGE.first, finalYRange.first, along)
		val high = MathUtil.lerp(VerticalBorder.WORLD_Y_RANGE.last, finalYRange.last, along)
		return low..high
	}

	fun start(activeGame: ActiveGame) {
		UHCBorder.set(activeGame.gameWorld, activeGame.finalRadius)
		Broadcast.broadcast { player ->
			if (player.world === activeGame.gameWorld) null
			else MSG.game("Endgame starting")
		}

		val netherTeamGroups = activeGame.teams.teams.mapNotNull { team ->
			val netherPlayersList = team.members.mapNotNull { playerData ->
				if (!playerData.isActive) return@mapNotNull null
				val block = playerData.getEntity()?.location?.block ?: return@mapNotNull null
				if (block.world !== activeGame.netherWorld) return@mapNotNull null
				playerData to block
			}
			val block = netherPlayersList.randomOrNull()?.second ?: return@mapNotNull null
			netherPlayersList.map { (playerData) -> playerData } to block
		}

		netherTeamGroups.forEach { (playerDatas, fromBlock) ->
			val exitBlock = PortalFix.getPortalExitLocation(activeGame.gameWorld, fromBlock)

			PortalFix.buildNetherPortal(exitBlock, Axis.X)

			playerDatas.forEach { playerData ->
				playerData.executeAction { player ->
					player.teleport(exitBlock.location.add(0.5, 0.0, 0.5))
				}.onZombie { zombie ->
					zombie.teleport(exitBlock.location.add(0.5, 0.0, 0.5))
				}
			}
		}

		activeGame.netherWorld.players.filter { it.gameMode === GameMode.SPECTATOR }.forEach { player ->
			player.teleport(activeGame.gameWorld.spawnLocation)
		}
	}

	fun tick(activeGame: ActiveGame, phaseAlong: ActiveGame.PhaseAlong) {
		val yRange = getAlongYRange(phaseAlong.along)

		verticalBorder.lowLevels(yRange).forEach { y ->
			if (verticalBorder.isFilled(y)) return@forEach
			VerticalBorder.buildBedrockLayer(activeGame.gameWorld, activeGame.finalRadius, y)
			verticalBorder.fill(y)
		}

		verticalBorder.highLevels(yRange).forEach { y ->
			if (verticalBorder.isFilled(y)) return@forEach
			VerticalBorder.removeSkyLayer(activeGame.gameWorld, activeGame.finalRadius, y)
			verticalBorder.fill(y)
		}

		VerticalBorder.pushPlayersUp(activeGame.gameWorld, activeGame.finalRadius, yRange.first)

		val timers = warningTimers.get()

		activeGame.playerDatas.active.forEach { playerData ->
			val entity = playerData.getEntity() ?: return@forEach
			val timer = timers.find { result -> result.timer.uuid == playerData.uuid }

			if (
				!(entity is Player && !PlayerManip.isSquishy(entity)) &&
				entity.world === activeGame.gameWorld &&
				entity.y > yRange.last + 1
			) {
				if (timer == null) {
					warningTimers.add(YDamageTimer(playerData.uuid))
				} else {
					if (timer.ticks >= DAMAGE_TIME && timer.ticks % 20 == 0) {
						UHCBorder.doDamage(entity)
					}
				}
			} else {
				timer?.let { warningTimers.remove(it.timer) }
			}

			if (glowingTimer == TickTime.ofSeconds(18)) {
				entity.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, TickTime.ofSeconds(2), 1))
			}
		}
	}

	fun postTick() {
		++glowingTimer
		if (glowingTimer >= TickTime.ofSeconds(20)) glowingTimer = 0
		warningTimers.postTick()
	}

	companion object {
		val DAMAGE_TIME = TickTime.ofSeconds(15)
	}
}
