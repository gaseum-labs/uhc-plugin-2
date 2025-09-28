package org.gaseumlabs.uhcplugin.core

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.gaseumlabs.uhcplugin.world.YFinder
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
		val random = Random(world.seed)

		val angleOffset = random.nextDouble(PI * 2.0)

		val playerToLocation = HashMap<UUID, Location>()

		for (group in groups) {
			val locations = getGroupLocations(
				world,
				angleOffset,
				config,
				groups.size,
				group.size,
				outerRadius,
				random
			)
				?: throw Exception("Could not find enough spread locations")

			for (playerIndex in group.indices) {
				val playerUUID = group[playerIndex]
				playerToLocation[playerUUID] = locations[playerIndex]
			}
		}

		return playerToLocation
	}

	data class Coord(val x: Int, val z: Int)

	fun getSingleLocation(playerUuid: UUID, world: World, outerRadius: Int, config: Config): Location? {
		val random = Random(world.seed.xor(world.time))

		val playerLocations = UHC.getGame()?.playerDatas?.active?.mapNotNull { playerData ->
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

		if (playerLocations == null || playerLocations.isEmpty()) {
			for ((x, z) in candidateCoords) {
				val location = findLocationsAt(world, config, x, z, 1, random)?.firstOrNull()
				if (location != null) return location
			}
			return null
		} else {
			val coordAndMinDistances = candidateCoords.map { coord ->
				coord to playerLocations.minOf { playerLocation ->
					distance2(
						playerLocation.x,
						playerLocation.z,
						coord.x + 0.5,
						coord.z + 0.5,
					)
				}
			}.sortedByDescending { (_, distance) -> distance }

			for ((coord) in coordAndMinDistances) {
				val location = findLocationsAt(world, config, coord.x, coord.z, 1, random)?.firstOrNull()
				if (location != null) return location
			}
			return null
		}
	}

	fun distance2(x0: Double, y0: Double, x1: Double, y1: Double): Double =
		(x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0)

	fun getGroupLocations(
		world: World,
		angleOffset: Double,
		config: Config,
		numGroups: Int,
		groupSize: Int,
		outerRadius: Int,
		random: Random,
	): List<Location>? {
		for (tryIndex in 0..<config.numTries) {
			val (x, z) = getSliceXZ(angleOffset, config, numGroups, outerRadius, random)

			val locations = findLocationsAt(world, config, x, z, groupSize, random)

			return locations
		}
		return null
	}

	fun getSliceXZ(
		angleOffset: Double,
		config: Config,
		numGroups: Int,
		outerRadius: Int,
		random: Random,
	): Pair<Int, Int> {
		val minRadius = (config.minDistance * numGroups) / (2.0 * PI);
		val maxAngle = (2.0 * PI) / numGroups

		val radius = random.nextDouble(minRadius, outerRadius.toDouble() - config.tryRadius)

		val angleVariance = (2.0 * PI) / numGroups - config.minDistance / radius

		val angle = angleOffset + random.nextDouble(-0.5, 0.5) * angleVariance

		val x = (cos(angle) * radius).roundToInt()
		val z = (sin(angle) * radius).roundToInt()

		return x to z
	}

	fun findLocationsAt(
		world: World,
		config: Config,
		x: Int,
		z: Int,
		numLocations: Int,
		random: Random,
	): List<Location>? {
		val locations = ArrayList<Location>()

		val y = YFinder.findTopBlockY(world, x, z) ?: return null

		val indexReorder = IntArray(config.tryRadius * 2 + 1) { i -> i }
		indexReorder.shuffle(random)


		for (ix in 0..<config.tryRadius * 2 + 1) {
			for (iy in 0..<config.tryRadius * 2 + 1) {
				for (iz in 0..<config.tryRadius * 2 + 1) {
					val ox = indexReorder[ix] - config.tryRadius
					val oy = indexReorder[iy] - config.tryRadius
					val oz = indexReorder[iz] - config.tryRadius

					val block = world.getBlockAt(x + ox, y + oy, z + oz)

					if (YFinder.isSurfaceBlock(block)) {
						val above = block.getRelative(0, 1, 0)

						if (canSpawnIn(above) && canSpawnIn(block.getRelative(0, 2, 0))) {
							locations.add(above.location.add(0.5, 0.0, 0.5))
							if (locations.size == numLocations) return locations
						}
					}
				}
			}
		}

		return null
	}

	fun canSpawnIn(block: Block): Boolean {
		return block.isPassable && when (block.type) {
			Material.WATER,
			Material.LAVA,
			Material.POWDER_SNOW,
			Material.TALL_GRASS,
			Material.SUNFLOWER,
			Material.LARGE_FERN,
			Material.PEONY,
			Material.ROSE_BUSH,
			Material.LILAC,
				-> false

			else -> true
		}
	}
}