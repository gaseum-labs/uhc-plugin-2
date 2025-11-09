package org.gaseumlabs.uhcplugin.regenResource

import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockType
import org.bukkit.loot.LootTables
import org.gaseumlabs.uhcplugin.core.broadcast.MSG
import org.gaseumlabs.uhcplugin.util.MathUtil
import org.gaseumlabs.uhcplugin.world.SurfaceFinder
import org.gaseumlabs.uhcplugin.world.YFinder
import kotlin.random.Random
import kotlin.random.nextInt

object RegenResources {
	data class ChestPlacement(val block: Block, val freeDirection: BlockFace)

	val fortressChest = object : RegenResource(World.Environment.NETHER, -10..10, -10..10, 3, 1.0 / 6.0) {
		override fun setBlock(chunk: Chunk): Block? {
			val (block, freeDirection) = findChestPlacement(chunk) ?: return null

			val chest = BlockType.CHEST.createBlockData()
			chest.facing = freeDirection
			block.setBlockData(chest, false)

			val chestState = block.state as org.bukkit.block.Chest
			chestState.setLootTable(LootTables.NETHER_BRIDGE.lootTable, Random.nextLong())
			chestState.customName(MSG.game("UHC Fortress Loot"))
			chestState.update()

			return block
		}

		fun findChestPlacement(chunk: Chunk): ChestPlacement? {
			val world = chunk.world
			for (tryIndex in 0..<16) {
				val x = Random.nextInt(0..15)
				val z = Random.nextInt(0..15)

				val startY = Random.nextInt(0..<30)
				val yDirection = Random.nextInt(0..1) * 2 - 1

				for (yIndex in 0..<30) {
					val y = MathUtil.posMod(startY + yIndex * yDirection, 30) + 50

					val block = chunk.getBlock(x, y, z)
					if (!block.isPassable) continue
					if (chunk.getBlock(x, y - 1, z).type !== Material.NETHER_BRICKS) continue
					val closedNorth =
						world.getBlockAt(chunk.x * 16 + x, y, chunk.z * 16 + z - 1).type === Material.NETHER_BRICKS
					val closedEast =
						world.getBlockAt(chunk.x * 16 + x + 1, y, chunk.z * 16 + z).type === Material.NETHER_BRICKS
					val closedSouth =
						world.getBlockAt(chunk.x * 16 + x, y, chunk.z * 16 + z + 1).type === Material.NETHER_BRICKS
					val closedWest =
						world.getBlockAt(chunk.x * 16 + x - 1, y, chunk.z * 16 + z).type === Material.NETHER_BRICKS

					if (!closedNorth && !closedEast && !closedSouth && !closedWest) break

					val openFace = when {
						closedNorth && !closedSouth -> BlockFace.SOUTH
						closedEast && !closedWest -> BlockFace.WEST
						closedSouth && !closedNorth -> BlockFace.NORTH
						else -> BlockFace.EAST
					}

					for (cy in y + 2..y + 5) {
						if (!chunk.getBlock(x, cy, z).isPassable) return ChestPlacement(
							block,
							openFace
						)
					}
					break
				}
			}
			return null
		}

		override fun isUntouched(block: Block): Boolean {
			return block.type === Material.CHEST
		}
	}

	val melon = object : RegenResource(World.Environment.NORMAL, -23..24, -23..24, 5, 1.0 / 32.0) {
		override fun setBlock(chunk: Chunk): Block? {
			val world = chunk.world

			val x = Random.nextInt(0..15)
			val z = Random.nextInt(0..15)

			val surfaceBlock = YFinder.findSurfaceBlock(world, chunk.x * 16 + x, chunk.z * 16 + z)

			val biome = surfaceBlock.biome
			when (biome) {
				Biome.JUNGLE,
				Biome.SPARSE_JUNGLE,
				Biome.BAMBOO_JUNGLE,
				Biome.FOREST,
				Biome.FLOWER_FOREST,
				Biome.BIRCH_FOREST,
				Biome.OLD_GROWTH_BIRCH_FOREST,
				Biome.DARK_FOREST,
				Biome.PALE_GARDEN,
				Biome.MANGROVE_SWAMP,
				Biome.WOODED_BADLANDS,
					-> {
				}

				else -> return null
			}

			val surfaceBlocks = SurfaceFinder.getSurfaceBlocks(world, surfaceBlock.x, surfaceBlock.z, 3)

			val dirtBlock = surfaceBlocks.firstOrNull { (block, tree) ->
				!tree && block.type === Material.GRASS_BLOCK
			}?.block ?: return null

			val melonBlock = dirtBlock.getRelative(BlockFace.UP)

			dirtBlock.setType(Material.DIRT, false)
			melonBlock.setType(Material.MELON, false)

			return melonBlock
		}

		override fun isUntouched(block: Block): Boolean {
			return block.type === Material.MELON
		}
	}

	val sugarcane = object : RegenResource(World.Environment.NORMAL, -23..24, -23..24, 5, 1.0 / 32.0) {
		override fun setBlock(chunk: Chunk): Block? {
			val world = chunk.world

			val x = Random.nextInt(0..15)
			val z = Random.nextInt(0..15)

			val surfaceBlock = YFinder.findSurfaceBlock(world, chunk.x * 16 + x, chunk.z * 16 + z)

			val surfaceBlocks = SurfaceFinder.getSurfaceBlocks(world, surfaceBlock.x, surfaceBlock.z, 3)

			val sandBlock = surfaceBlocks.firstOrNull { (block, tree) ->
				(block.type === Material.SAND || block.type === Material.GRASS_BLOCK) && isNextToWater(block)
			}?.block ?: return null

			var caneBlock = sandBlock

			for (i in 1..3) {
				caneBlock = caneBlock.getRelative(BlockFace.UP)
				caneBlock.setType(Material.SUGAR_CANE, false)
			}

			return caneBlock
		}

		override fun isUntouched(block: Block): Boolean {
			return block.type === Material.SUGAR_CANE
		}

		private fun isNextToWater(block: Block): Boolean {
			val faces = listOf(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)
			return faces.any { face ->
				val relative = block.getRelative(face)
				relative.type == Material.WATER || relative.type == Material.ICE
			}
		}
	}

	val list = listOf(fortressChest, melon, sugarcane)
}