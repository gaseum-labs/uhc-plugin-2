package org.gaseumlabs.uhcplugin.world

import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.type.Snow

object YFinder {
	fun findTopBlockY(world: World, x: Int, z: Int): Int? {
		for (y in 255 downTo 0) {
			val block = world.getBlockAt(x, y, z)
			if (isSurfaceBlock(block)) return y
		}
		return null
	}

	fun findTopBlockY(world: World, x: Int, z: Int, range: IntRange): Int {
		for (y in range.last downTo range.first + 1) {
			val block = world.getBlockAt(x, y, z)
			if (isSurfaceBlock(block)) return y
		}
		return range.first
	}

	fun findTopBlockY(chunk: Chunk, x: Int, z: Int): Int? {
		for (y in 255 downTo 0) {
			val block = chunk.getBlock(x, y, z)
			if (isSurfaceBlock(block)) return y
		}
		return null
	}

	fun findSurfaceBlock(world: World, x: Int, z: Int): Block {
		for (y in 255 downTo -64) {
			val block = world.getBlockAt(x, y, z)
			if (isSurfaceBlock(block)) return block
		}
		return world.getBlockAt(x, -64, z)
	}

	fun isLiquidBlock(block: Block): Boolean {
		return when (block.type) {
			Material.WATER,
			Material.LAVA,
			Material.POWDER_SNOW,
				-> true

			else -> false
		}
	}

	fun isTreeBlock(block: Block): Boolean {
		return when (block.type) {
			Material.OAK_LEAVES,
			Material.OAK_LOG,
			Material.BIRCH_LEAVES,
			Material.BIRCH_LOG,
			Material.SPRUCE_LEAVES,
			Material.SPRUCE_LOG,
			Material.DARK_OAK_LEAVES,
			Material.DARK_OAK_LOG,
			Material.CHERRY_LEAVES,
			Material.CHERRY_LOG,
			Material.ACACIA_LEAVES,
			Material.ACACIA_LOG,
			Material.JUNGLE_LEAVES,
			Material.JUNGLE_LOG,
			Material.MANGROVE_LEAVES,
			Material.MANGROVE_LOG,
			Material.AZALEA_LEAVES,
			Material.FLOWERING_AZALEA_LEAVES,
			Material.BEE_NEST,
			Material.BAMBOO,
			Material.PUMPKIN,
			Material.MELON,
			Material.POINTED_DRIPSTONE,
			Material.PACKED_ICE,
			Material.LILY_PAD,
				-> true

			Material.SNOW,
				-> (block.blockData as Snow).layers > 1

			else -> false
		}
	}

	fun isSurfaceBlock(block: Block): Boolean {
		return isLiquidBlock(block) || (!block.isPassable && !isTreeBlock(block))
	}
}
