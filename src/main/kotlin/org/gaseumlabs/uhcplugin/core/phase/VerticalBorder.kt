package org.gaseumlabs.uhcplugin.core.phase

import io.papermc.paper.entity.TeleportFlag
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Container
import org.gaseumlabs.uhcplugin.core.PlayerManip
import org.gaseumlabs.uhcplugin.world.YFinder

class VerticalBorder {
	private val filledLevels = BooleanArray(WORLD_Y_RANGE.count())

	fun lowLevels(yRange: IntRange): IntRange {
		return WORLD_Y_RANGE.first..<yRange.first
	}

	fun highLevels(yRange: IntRange): IntRange {
		return yRange.last + 1..WORLD_Y_RANGE.last
	}

	fun isFilled(y: Int): Boolean {
		return filledLevels[y - WORLD_Y_RANGE.first]
	}

	fun fill(y: Int) {
		filledLevels[y - WORLD_Y_RANGE.first] = true
	}

	companion object {
		val WORLD_Y_RANGE = -64..319
		val FIND_Y_RANGE = 60..255

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

		private fun breakContainer(world: World, x: Int, y: Int, z: Int) {
			val tileEntity = world.getBlockState(x, y, z)
			if (tileEntity is Container) {
				tileEntity.inventory.forEach { itemStack ->
					if (itemStack == null) return@forEach
					world.dropItemNaturally(tileEntity.location.add(0.5, 0.5, 0.5), itemStack)
				}
			}
		}

		fun buildBedrockLayer(world: World, radius: Int, y: Int) {
			for (x in -radius..radius) {
				for (z in -radius..radius) {
					breakContainer(world, x, y, z)
					world.getBlockAt(x, y, z).setType(Material.BEDROCK, false)
				}
			}
		}

		fun removeSkyLayer(world: World, radius: Int, y: Int) {
			for (x in -radius..radius) {
				for (z in -radius..radius) {
					breakContainer(world, x, y, z)
					world.getBlockAt(x, y, z).setType(Material.AIR, false)
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
