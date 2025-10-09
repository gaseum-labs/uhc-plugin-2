package org.gaseumlabs.uhcplugin.world

import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import kotlin.math.absoluteValue

object SurfaceFinder {
	data class SurfaceBlock(val block: Block, val tree: Boolean, val liquid: Boolean)

	fun getSurfaceBlocks(world: World, x: Int, z: Int, radius: Int): Array<SurfaceBlock> {
		val middleBlock = YFinder.findSurfaceBlock(world, x, z)

		val trail0 = getSurfaceBlocksDirection(middleBlock, 1, 0, radius)
		val trail1 = getSurfaceBlocksDirection(middleBlock, 0, 1, radius)
		val trail2 = getSurfaceBlocksDirection(middleBlock, -1, 0, radius)
		val trail3 = getSurfaceBlocksDirection(middleBlock, 0, -1, radius)
		val trail4 = getSurfaceBlocksDirection(middleBlock, 1, 1, radius)
		val trail5 = getSurfaceBlocksDirection(middleBlock, 1, -1, radius)
		val trail6 = getSurfaceBlocksDirection(middleBlock, -1, -1, radius)
		val trail7 = getSurfaceBlocksDirection(middleBlock, -1, 1, radius)

		val surfaceBlocks = Array(radius * 8 + 1) { index ->
			when {
				index == 0 -> blockToSurfaceBlock(middleBlock)
				index < radius + 1 -> blockToSurfaceBlock(trail0[index - 1])
				index < radius * 2 + 1 -> blockToSurfaceBlock(trail1[index - (radius + 1)])
				index < radius * 3 + 1 -> blockToSurfaceBlock(trail2[index - (radius * 2 + 1)])
				index < radius * 4 + 1 -> blockToSurfaceBlock(trail3[index - (radius * 3 + 1)])
				index < radius * 5 + 1 -> blockToSurfaceBlock(trail4[index - (radius * 4 + 1)])
				index < radius * 6 + 1 -> blockToSurfaceBlock(trail5[index - (radius * 5 + 1)])
				index < radius * 7 + 1 -> blockToSurfaceBlock(trail6[index - (radius * 6 + 1)])
				else -> blockToSurfaceBlock(trail7[index - (radius * 7 + 1)])
			}
		}

		surfaceBlocks.sortBy { surfaceBlock ->
			((surfaceBlock.block.y - 63).absoluteValue.coerceAtMost(63)) +
				(if (surfaceBlock.tree) 64 else 0) +
				(if (surfaceBlock.liquid) 128 else 0)
		}

		return surfaceBlocks
	}

	private fun blockToSurfaceBlock(block: Block): SurfaceBlock {
		val tree = YFinder.isTreeBlock(block.getRelative(BlockFace.UP))
		val liquid = YFinder.isLiquidBlock(block)
		return SurfaceBlock(block, tree, liquid)
	}

	private fun getSurfaceBlocksDirection(block: Block, dx: Int, dz: Int, radius: Int): ArrayList<Block> {
		val trail = ArrayList<Block>()

		var lastY = block.y
		val world = block.world

		for (r in 1..radius) {
			val x = block.x + r * dx
			val z = block.z + r * dz

			val initial = world.getBlockAt(x, lastY, z)

			val nextSurface: Block = if (YFinder.isSurfaceBlock(initial)) {
				val airAbove = findNextAirBlockAbove(initial)

				val superSurface = findNextSurfaceBlockAbove(airAbove, 20)

				if (superSurface != null) {
					val superAir = findNextAirBlockAbove(superSurface)

					superAir.getRelative(BlockFace.DOWN)
				} else {
					airAbove.getRelative(BlockFace.DOWN)
				}
			} else {
				val surfaceAbove = findNextSurfaceBlockAbove(initial, 20)
				if (surfaceAbove != null) {
					val airAbove = findNextAirBlockAbove(surfaceAbove)

					airAbove.getRelative(BlockFace.DOWN)
				} else {
					val surfaceBelow = findNextSurfaceBlockBelow(initial)

					surfaceBelow
				}
			}

			trail.add(nextSurface)
			lastY = nextSurface.y
		}

		return trail
	}

	private fun findNextAirBlockAbove(block: Block): Block {
		val world = block.world

		for (aboveY in block.y + 1..255) {
			val aboveBlock = world.getBlockAt(block.x, aboveY, block.z)
			if (!YFinder.isSurfaceBlock(aboveBlock)) {
				return aboveBlock
			}
		}

		return world.getBlockAt(block.x, 256, block.z)
	}

	private fun findNextSurfaceBlockAbove(block: Block, limitSearch: Int): Block? {
		val world = block.world

		for (aboveY in block.y + 1..255) {
			val aboveBlock = world.getBlockAt(block.x, aboveY, block.z)
			if (YFinder.isSurfaceBlock(aboveBlock)) {
				return aboveBlock
			}
		}

		return null
	}

	private fun findNextSurfaceBlockBelow(block: Block): Block {
		val world = block.world

		for (belowY in block.y - 1 downTo -64) {
			val belowBlock = world.getBlockAt(block.x, belowY, block.z)
			if (YFinder.isSurfaceBlock(belowBlock)) {
				return belowBlock
			}
		}

		return world.getBlockAt(block.x, -64, block.z)
	}

	fun findAirAboveTree(surfaceBlock: Block): Block {
		val world = surfaceBlock.world

		for (aboveY in surfaceBlock.y + 1..255) {
			val aboveBlock = world.getBlockAt(surfaceBlock.x, aboveY, surfaceBlock.z)
			if (!YFinder.isSurfaceBlock(aboveBlock)) return aboveBlock
		}

		return world.getBlockAt(surfaceBlock.x, 256, surfaceBlock.z)
	}
}
