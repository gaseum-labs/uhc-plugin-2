package org.gaseumlabs.uhcplugin.fix

import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.bukkit.*
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.ItemType
import org.gaseumlabs.uhcplugin.UHCPlugin
import org.gaseumlabs.uhcplugin.world.SurfaceFinder
import org.gaseumlabs.uhcplugin.world.YFinder
import kotlin.random.Random
import kotlin.random.nextInt

class MelonFix : Listener {
	val chunkQueue = ArrayList<Chunk>()

	@EventHandler
	fun onGenerate(event: ChunkLoadEvent) {
		if (!event.isNewChunk) return
		val chunk = event.chunk
		if (chunk.world.environment !== World.Environment.NORMAL) return

		val random = Random.Default

		Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.self) {
			chunkQueue.removeIf { chunk ->
				val isReady = isChunkReady(chunk)
				if (isReady)
					generateMelon(chunk, random)
				isReady
			}
		}


		if (random.nextInt(32) == 0) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.self) {
				chunkQueue.add(chunk)
			}
		}
	}

	@EventHandler
	fun onDropPlayer(event: BlockBreakEvent) {
		if (event.block.type === Material.MELON) {
			if (event.isDropItems && event.player.gameMode !== GameMode.CREATIVE) {
				event.isDropItems = false
				event.block.world.dropItemNaturally(
					event.block.location.add(0.5, 0.5, 0.5),
					ItemType.MELON_SLICE.createItemStack(1)
				)
			}
		}
	}

	@EventHandler
	fun onDropNatural(event: BlockBreakBlockEvent) {
		if (event.block.type === Material.MELON) {
			event.drops.clear()
			event.drops.add(ItemType.MELON_SLICE.createItemStack(1))
		}
	}

	fun generateMelon(chunk: Chunk, random: Random) {
		val world = chunk.world

		val x = random.nextInt(0..15)
		val z = random.nextInt(0..15)

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

			else -> return
		}

		val surfaceBlocks = SurfaceFinder.getSurfaceBlocks(world, surfaceBlock.x, surfaceBlock.z, 3)

		val generateBlock = surfaceBlocks.firstOrNull { (block, tree) ->
			!tree && block.type === Material.GRASS_BLOCK
		}?.block ?: return

		generateBlock.setType(Material.DIRT, false)
		generateBlock.getRelative(BlockFace.UP).setType(Material.MELON, false)
	}

	fun isChunkReady(chunk: Chunk): Boolean {
		for (x in -1..1) {
			for (z in -1..1) {
				if (!chunk.world.isChunkGenerated(chunk.x + x, chunk.z + z)) return false
			}
		}
		return true
	}
}
