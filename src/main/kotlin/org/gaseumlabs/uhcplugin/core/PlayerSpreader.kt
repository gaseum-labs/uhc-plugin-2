package org.gaseumlabs.uhcplugin.core

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
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
		val random = Random.Default

		val angleOffset = random.nextDouble(PI * 2.0)

		val playerToLocation = HashMap<UUID, Location>()

		groups.forEachIndexed { groupIndex, group ->
			val locations = ArrayList<Location>()

			for (tryIndex in 0..<config.numTries) {
				val (x, z) = getSliceXZ(config, angleOffset, groups.size, groupIndex, outerRadius, random)

				findLocationsAt(world, config, x, z, group.size, random)?.let {
					locations.addAll(it)
					break
				}
			}

			if (locations.isEmpty()) throw Exception("Could not find enough spread locations")

			group.forEachIndexed { index, uuid ->
				playerToLocation[uuid] = locations[index]
			}
		}

		return playerToLocation
	}

	data class Coord(val x: Int, val z: Int)

	fun getSingleLocation(
		activeGame: ActiveGame,
		playerUuid: UUID,
		world: World,
		outerRadius: Int,
		config: Config,
	): Location? {
		val random = Random(world.seed.xor(world.time))

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

		if (playerLocations.isEmpty()) {
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

	fun getSliceXZ(
		config: Config,
		angleOffset: Double,
		numGroups: Int,
		groupIndex: Int,
		outerRadius: Int,
		random: Random,
	): Pair<Int, Int> {
		val minRadius = (config.minDistance * numGroups) / (2.0 * PI);
		val maxAngle = (2.0 * PI) / numGroups

		val radius = random.nextDouble(minRadius, outerRadius.toDouble() - config.tryRadius)

		val angleVariance = (2.0 * PI) / numGroups - config.minDistance / radius

		val angle = groupIndex * maxAngle + angleOffset + random.nextDouble(-0.5, 0.5) * angleVariance

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
	): ArrayList<Location>? {
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

	fun getSurfaceBlocks(block: Block, dx: Int, dz: Int, radius: Int): ArrayList<Block> {
		val trail = ArrayList<Block>()

		var lastY = block.y
		val world = block.world

		for (r in 1..radius) {
			val x = block.x + r * dx
			val z = block.z + r * dz

			val current = world.getBlockAt(x, lastY, z)

			val nextSurface: Block = if (YFinder.isSurfaceBlock(current)) {
				val airAbove = findNextAirBlockAbove(current)

				val superSurface = findNextSurfaceBlockAbove(airAbove, 20)

				if (superSurface != null) {
					val superAir = findNextAirBlockAbove(superSurface)

					superAir.getRelative(BlockFace.DOWN)
				} else {
					airAbove.getRelative(BlockFace.DOWN)
				}
			} else {
				val surfaceAbove = findNextSurfaceBlockAbove(current, 20)
				if (surfaceAbove != null) {
					val airAbove = findNextAirBlockAbove(surfaceAbove)

					airAbove.getRelative(BlockFace.DOWN)
				} else {
					val surfaceBelow = findNextSurfaceBlockBelow(current)

					surfaceBelow
				}
			}

			trail.add(nextSurface)
			lastY = nextSurface.y
		}

		return trail
	}

	fun findNextAirBlockAbove(block: Block): Block {
		val world = block.world

		for (aboveY in block.y + 1..255) {
			val aboveBlock = world.getBlockAt(block.x, aboveY, block.z)
			if (!YFinder.isSurfaceBlock(aboveBlock)) {
				return aboveBlock
			}
		}

		return world.getBlockAt(block.x, 256, block.z)
	}

	fun findNextSurfaceBlockAbove(block: Block, limitSearch: Int): Block? {
		val world = block.world

		for (aboveY in block.y + 1..255) {
			val aboveBlock = world.getBlockAt(block.x, aboveY, block.z)
			if (YFinder.isSurfaceBlock(aboveBlock)) {
				return aboveBlock
			}
		}

		return null
	}

	fun findNextSurfaceBlockBelow(block: Block): Block {
		val world = block.world

		for (belowY in block.y - 1 downTo -64) {
			val belowBlock = world.getBlockAt(block.x, belowY, block.z)
			if (YFinder.isSurfaceBlock(belowBlock)) {
				return belowBlock
			}
		}

		return world.getBlockAt(block.x, -64, block.z)
	}
}
