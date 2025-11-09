package org.gaseumlabs.uhcplugin.core

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import org.gaseumlabs.uhcplugin.core.playerData.PlayerData
import org.gaseumlabs.uhcplugin.world.SurfaceFinder
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlin.random.nextInt

object PlayerSpreader {
	data class Config(
		val tryRadius: Int,
		val numTries: Int,
		val minDistance: Int,
	)

	val CONFIG_DEFAULT = Config(tryRadius = 7, numTries = 25, minDistance = 75)

	fun getPlayerLocations(
		world: World,
		config: Config,
		outerRadius: Int,
		groups: List<List<UUID>>,
	): HashMap<UUID, Location> {
		val random = Random.Default

		val angleOffset = random.nextDouble(PI * 2.0)

		val playerToLocation = HashMap<UUID, Location>()

		groups.forEachIndexed { groupIndex, group ->
			val (x, z) = getSliceXZ(config, angleOffset, groups.size, groupIndex, outerRadius, random)

			fillPlayerToLocationXZ(config, world, playerToLocation, group, x, z)
		}

		return playerToLocation
	}

	private fun fillPlayerToLocationXZ(
		config: Config,
		world: World,
		playerToLocation: HashMap<UUID, Location>,
		group: List<UUID>,
		x: Int,
		z: Int,
	) {
		val surfaceBlocks = SurfaceFinder.getSurfaceBlocks(world, x, z, config.tryRadius)

		surfaceBlocks.take(group.size).forEachIndexed { index, (block, tree, liquid) ->
			val uuid = group[index]

			val airBlock = if (tree) {
				val airAbove = SurfaceFinder.findAirAboveTree(block)
				airAbove
			} else if (liquid) {
				block.setType(Material.STONE, true)
				block.getRelative(BlockFace.UP)
			} else {
				block.getRelative(BlockFace.UP)
			}

			playerToLocation[uuid] = airBlock.location.add(0.5, 0.0, 0.5)
		}
	}

	data class Coord(val x: Int, val z: Int)

	fun getSingleLocation(
		activeGame: ActiveGame,
		playerUuid: UUID,
		world: World,
		outerRadius: Int,
		config: Config,
	): Location {
		val random = Random.Default

		val playerLocations = activeGame.playerDatas.active.mapNotNull { playerData ->
			if (playerUuid == playerData.uuid) return@mapNotNull null
			val location = playerData.getEntity()?.location ?: return@mapNotNull null
			if (location.world === world) location else null
		}

		val candidateCoords = Array(config.numTries) {
			Coord(
				random.nextInt(-outerRadius + config.tryRadius..outerRadius - config.tryRadius),
				random.nextInt(-outerRadius + config.tryRadius..outerRadius - config.tryRadius)
			)
		}

		val playerToLocation = HashMap<UUID, Location>()

		val coord = if (playerLocations.isEmpty()) {
			candidateCoords[0]
		} else {
			candidateCoords.maxBy { coord ->
				playerLocations.minOf { playerLocation ->
					distance2(
						playerLocation.x,
						playerLocation.z,
						coord.x + 0.5,
						coord.z + 0.5,
					)
				}
			}
		}

		fillPlayerToLocationXZ(config, world, playerToLocation, listOf(playerUuid), coord.x, coord.z)

		return playerToLocation[playerUuid]!!
	}

	fun getRespawnLocation(
		activeGame: ActiveGame,
		currentPlayerData: PlayerData,
		world: World,
		outerRadius: Int,
		respawnRadius: Int,
		config: Config,
	): Location {
		val random = Random.Default

		val playerLocations = activeGame.playerDatas.active.mapNotNull { playerData ->
			if (playerData == currentPlayerData) return@mapNotNull null
			val location = playerData.getEntity()?.location ?: return@mapNotNull null
			if (location.world === world) location else null
		}
		val player = currentPlayerData.getPlayer()!!
		val currentX = player.location.x
		val currentZ = player.location.z
		val candidateCoords = Array(config.numTries) {
			val dx = random.nextInt(-respawnRadius + config.tryRadius..respawnRadius - config.tryRadius)
			val dz = random.nextInt(-respawnRadius + config.tryRadius..respawnRadius - config.tryRadius)

			// Clamp within the total outerRadius boundary (centered at origin)
			val newX = (currentX + dx).coerceIn((-outerRadius + config.tryRadius).toDouble(),
				(outerRadius - config.tryRadius).toDouble()
			)
			val newZ = (currentZ + dz).coerceIn((-outerRadius + config.tryRadius).toDouble(),
				(outerRadius - config.tryRadius).toDouble()
			)

			Coord(newX.toInt(), newZ.toInt())
		}

		val playerToLocation = HashMap<UUID, Location>()

		val coord = if (playerLocations.isEmpty()) {
			candidateCoords[0]
		} else {
			candidateCoords.maxBy { coord ->
				playerLocations.minOf { playerLocation ->
					distance2(
						playerLocation.x,
						playerLocation.z,
						coord.x + 0.5,
						coord.z + 0.5,
					)
				}
			}
		}

		fillPlayerToLocationXZ(config, world, playerToLocation, listOf(currentPlayerData.uuid), coord.x, coord.z)

		return playerToLocation[currentPlayerData.uuid]!!
	}

	fun distance2(x0: Double, y0: Double, x1: Double, y1: Double): Double =
		(x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0)

	fun getSliceXZ(
		config: Config,
		angleOffset: Double,
		numGroups: Int,
		groupIndex: Int,
		outerRadius: Int,
		random: Random,
	): Pair<Int, Int> {
		val maxAngle = (2.0 * PI) / numGroups

		val radius = random.nextDouble(
			outerRadius.toDouble() * 0.5 + config.tryRadius,
			outerRadius.toDouble() - config.tryRadius
		)

		val angleVariance = (2.0 * PI) / numGroups - config.minDistance / radius

		val angle = groupIndex * maxAngle + angleOffset + random.nextDouble(-0.5, 0.5) * angleVariance

		val x = (cos(angle) * radius).roundToInt()
		val z = (sin(angle) * radius).roundToInt()

		return x to z
	}
}
