package org.gaseumlabs.uhcplugin.world

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block

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

	fun isSurfaceBlock(block: Block): Boolean {
		if (block.isPassable) return false
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
			Material.POINTED_DRIPSTONE,
			Material.POWDER_SNOW,
			Material.PACKED_ICE,
			Material.SNOW_BLOCK,
			Material.LILY_PAD,
				-> false

			else -> true
		}
	}
}