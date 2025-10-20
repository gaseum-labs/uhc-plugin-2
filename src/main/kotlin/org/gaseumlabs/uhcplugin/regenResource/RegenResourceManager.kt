package org.gaseumlabs.uhcplugin.regenResource

import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import java.util.*
import kotlin.random.Random

class RegenResourceManager {
	data class Feature(val chunk: Chunk, val block: Block)
	data class TeamLoot(var numFound: Int)
	data class ResourceData(
		val resource: RegenResource,
		val chunkGrid: ChunkGrid,
		val features: ArrayList<Feature>,
		val teamLoots: HashMap<UUID, TeamLoot>,
	)

	val resourceDatas = RegenResources.list.map { regenResource ->
		ResourceData(
			regenResource,
			ChunkGrid(regenResource.xRange, regenResource.zRange),
			ArrayList(),
			HashMap()
		)
	}

	fun tick(timer: Int, activeGame: ActiveGame) {
		val resourceIndex = timer % 60
		val (regenResource, chunkGrid, features) = resourceDatas.getOrNull(resourceIndex) ?: return

		val world =
			if (regenResource.environment === World.Environment.NORMAL) activeGame.gameWorld else activeGame.netherWorld

		val worldPlayers = activeGame.playerDatas.active.mapNotNull { playerData ->
			val player = playerData.getPlayer() ?: return@mapNotNull null
			if (player.world !== world) return@mapNotNull null
			player
		}

		worldPlayers.forEach { player ->
			val chunk = player.chunk
			val cx = chunk.x
			val cz = chunk.z

			for (x in -regenResource.radius..regenResource.radius) {
				for (z in -regenResource.radius..regenResource.radius) {
					chunkGrid.set(cx + x, cz + z, ChunkGrid.IN_PLAY)
				}
			}
		}

		chunkGrid.forEach { x, z, oldValue, newValue ->
			if (oldValue == ChunkGrid.EMPTY && newValue == ChunkGrid.IN_PLAY) {
				if (Random.nextDouble() < regenResource.generateChance) {
					val chunk = world.getChunkAt(x, z)
					val block = regenResource.setBlock(chunk) ?: return@forEach

					features.add(Feature(chunk, block))

					//Broadcast.broadcast { MSG.success("created chest at ${block.x} ${block.y} ${block.z}") }
				}
			}
		}

		features.removeIf { feature ->
			val block = feature.block

			if (!regenResource.isUntouched(block)) {
				return@removeIf true
			}

			if (chunkGrid.get(block.chunk.x, block.chunk.z) != ChunkGrid.IN_PLAY) {
				block.setType(Material.AIR, false)
				return@removeIf true
			}

			false
		}

		chunkGrid.swap()
	}
}

