package org.gaseumlabs.uhcplugin.core.phase

import io.papermc.paper.block.TileStateInventoryHolder
import io.papermc.paper.entity.TeleportFlag
import org.bukkit.Axis
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
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
import org.gaseumlabs.uhcplugin.world.YFinder

class EndgamePhase(val finalYRange: IntRange) : Phase(PhaseType.ENDGAME) {
	var currentYRange: IntRange = WORLD_Y_RANGE
	var warningTimers = MultiTimerHolder<YDamageTimer>()

	fun getCurrentYRange(along: Double): IntRange {
		val low = MathUtil.lerp(WORLD_Y_RANGE.first, finalYRange.first, along)
		val high = MathUtil.lerp(WORLD_Y_RANGE.first, finalYRange.first, along)
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
		val oldYRange = this.currentYRange
		val newYRange = getCurrentYRange(phaseAlong.along)

		val newDestroyLows = oldYRange.first..<newYRange.first
		val newDestroyHighs = newYRange.last + 1..oldYRange.last

		newDestroyLows.forEach { y ->
			buildBedrockLayer(activeGame.gameWorld, activeGame.finalRadius, y)
		}
		newDestroyHighs.forEach { y ->
			removeSkyLayer(activeGame.gameWorld, activeGame.finalRadius, y)
		}

		pushPlayersUp(activeGame.gameWorld, activeGame.finalRadius, newYRange.first)

		this.currentYRange = newYRange

		val timers = warningTimers.get()

		activeGame.playerDatas.active.forEach { playerData ->
			val entity = playerData.getEntity() ?: return@forEach
			val timer = timers.find { result -> result.timer.uuid == playerData.uuid }

			if (
				!(entity is Player && !PlayerManip.isSquishy(entity)) &&
				entity.world === activeGame.gameWorld &&
				entity.y > newYRange.last
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
		}
	}

	companion object {
		val WORLD_Y_RANGE = -64..319
		val FIND_Y_RANGE = 60..255
		val DAMAGE_TIME = TickTime.ofSeconds(15)

		fun findMedianY(world: World, xRange: IntRange, zRange: IntRange): Int {
			val yCounts = IntArray(FIND_Y_RANGE.count())

			for (x in xRange) {
				for (z in zRange) {
					val y = YFinder.findTopBlockY(world, x, z, FIND_Y_RANGE)
					++yCounts[y - FIND_Y_RANGE.first]
				}
			}

			val numCounts = xRange.count() * zRange.count()
			val midPoint = numCounts / 2

			var count = 0
			for (i in yCounts.indices) {
				val y = i + FIND_Y_RANGE.first
				count += yCounts[i]

				if (midPoint < count) return y
			}
			return FIND_Y_RANGE.last
		}

		fun getFinalYRange(world: World, radius: Int, numYLevels: Int): IntRange {
			val medianY = findMedianY(world, -radius..radius, -radius..radius)

			val low = (medianY - numYLevels / 2).coerceAtLeast(FIND_Y_RANGE.first)
			val high = low + numYLevels - 1

			return low..high
		}

		fun buildBedrockLayer(world: World, radius: Int, y: Int) {
			for (x in -radius..radius) {
				for (z in -radius..radius) {
					val tileEntity = world.getBlockData(x, y, z)
					if (tileEntity is TileStateInventoryHolder) {
						tileEntity.inventory.forEach { itemStack ->
							world.dropItemNaturally(tileEntity.location.add(0.5, 0.5, 0.5), itemStack)
						}
					}
					world.getBlockAt(x, y, z).setType(Material.BEDROCK, false)
				}
			}
		}

		fun removeSkyLayer(world: World, radius: Int, y: Int) {
			for (x in -radius..radius) {
				for (z in -radius..radius) {
					val tileEntity = world.getBlockData(x, y, z)
					world.getBlockAt(x, y, z).setType(Material.BEDROCK, false)
				}
			}
		}

		fun pushPlayersUp(world: World, radius: Int, y: Int) {
			world.players.forEach { player ->
				if (!PlayerManip.isCollide(player)) return@forEach
				val block = player.location.block
				if (block.x !in -radius..radius || block.z !in -radius..radius) return@forEach

				if (player.y < y.toDouble()) {
					val playerLocation = player.location
					playerLocation.y = y.toDouble()
					player.teleport(
						playerLocation,
						TeleportFlag.EntityState.RETAIN_VEHICLE,
						TeleportFlag.Relative.VELOCITY_X,
						TeleportFlag.Relative.VELOCITY_Y,
						TeleportFlag.Relative.VELOCITY_Z,
						TeleportFlag.Relative.VELOCITY_ROTATION
					)
				}
			}
		}
	}
}
