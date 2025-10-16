package org.gaseumlabs.uhcplugin.lootRegen

import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockType
import org.bukkit.loot.LootTables
import org.gaseumlabs.uhcplugin.core.broadcast.Broadcast
import org.gaseumlabs.uhcplugin.core.broadcast.MSG
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import org.gaseumlabs.uhcplugin.core.timer.TickTime
import org.gaseumlabs.uhcplugin.util.WorldUtil
import java.util.*
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.nextInt

class LootRegen {
	data class Chest(val chunk: Chunk, val chest: Block, val teamUUID: UUID, val time: Int)

	data class TeamLoot(var numFound: Int)

	val MAX_FOUND_PER_TEAM = 4
	val MAX_CURRENT = 3
	val DESPAWN_RADIUS = 24.0
	val MAX_LOOT_TIME = TickTime.ofMinutes(2)

	val chests = ArrayList<Chest>()
	val teamLoots = HashMap<UUID, TeamLoot>()

	fun tick(timer: Int, activeGame: ActiveGame) {
		if (timer % 20 != 3) return

		val fortressTeams = activeGame.teams.teams.filter { team ->
			team.members.any { playerData ->
				val player = playerData.getPlayer() ?: return@any false
				if (player.world !== activeGame.netherWorld) return@any false
				val block = player.location.block
				WorldUtil.isFortressAt(activeGame.netherWorld, block.x, block.y, block.z)
			}
		}

		fortressTeams.forEach { team ->
			val teamLoot = teamLoots.getOrPut(team.uuid) { TeamLoot(0) }

			if (teamLoot.numFound >= MAX_FOUND_PER_TEAM) return@forEach

			val numTeamChests = chests.count { chest -> chest.teamUUID == team.uuid }

			if (numTeamChests >= MAX_CURRENT) return@forEach

			val player =
				team.members.shuffled().firstNotNullOfOrNull { playerData -> playerData.getPlayer() } ?: return@forEach
			val generateChunk = findAroundChunk(player.chunk) ?: return@forEach
			val (generateBlock, freeDirection) = findBlock(generateChunk) ?: return@forEach

			val chest = BlockType.CHEST.createBlockData()
			chest.facing = freeDirection
			generateBlock.setBlockData(chest, false)

			val chestState = generateBlock.state as org.bukkit.block.Chest
			chestState.lootTable = LootTables.NETHER_BRIDGE.lootTable

			Broadcast.broadcastGame(
				activeGame,
				MSG.success("chest generated at ${generateBlock.x} ${generateBlock.y} ${generateBlock.z}")
			)

			chests.add(Chest(generateChunk, generateBlock, team.uuid, timer))
		}

		val netherPlayers = activeGame.playerDatas.active.mapNotNull { playerData ->
			val player = playerData.getPlayer() ?: return@mapNotNull null
			if (player.world !== activeGame.netherWorld) return@mapNotNull null
			player
		}

		chests.removeIf { chest ->
			val shouldRemove = timer - chest.time > MAX_LOOT_TIME
			if (!shouldRemove) return@removeIf false

			val block = chest.chest
			val x0 = block.x + 0.5
			val y0 = block.y + 0.5
			val z0 = block.x + 0.5

			val minDistance = if (netherPlayers.isEmpty()) 99999.0 else netherPlayers.minOf { player ->
				val location = player.location

				val x1 = location.x + 0.5
				val y1 = location.y + 0.5
				val z1 = location.x + 0.5

				sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0) + (z1 - z0) * (z1 - z0))
			}

			if (minDistance < DESPAWN_RADIUS) return@removeIf false

			block.setType(Material.AIR, false)

			Broadcast.broadcastGame(
				activeGame,
				MSG.success("chest removed at ${block.x} ${block.y} ${block.z}")
			)

			true
		}
	}

	fun findAroundChunk(centerChunk: Chunk): Chunk? {
		for (tryIndex in 0..<16) {
			val space = Random.nextInt(0..3)
			val (dx, dz) = when (space) {
				0 -> Random.nextInt(-3..-2) to Random.nextInt(-3..3)
				1 -> Random.nextInt(-3..-2) to Random.nextInt(-3..3)
				2 -> Random.nextInt(-3..-2) to Random.nextInt(-3..3)
				else -> Random.nextInt(-3..-2) to Random.nextInt(-3..3)
			}
			val chunk = centerChunk.world.getChunkAt(centerChunk.x + dx, centerChunk.z + dz)
			if (chests.any { loot -> loot.chunk === chunk }) continue
			return chunk
		}
		return null
	}

	data class BlockPlacement(val block: Block, val freeDirection: BlockFace)

	fun findBlock(chunk: Chunk): BlockPlacement? {
		for (tryIndex in 0..<16) {
			val x = Random.nextInt(0..15)
			val z = Random.nextInt(0..15)

			val startY = Random.nextInt(0..<30)

			for (yIndex in 0..<30) {
				val y = (startY + yIndex) % 30 + 50

				val block = chunk.getBlock(x, y, z)
				if (!block.isPassable) continue
				if (chunk.getBlock(x, y - 1, z).type !== Material.NETHER_BRICKS) continue
				val closedNorth = chunk.getBlock(x, y, z - 1).type === Material.NETHER_BRICKS
				val closedEast = chunk.getBlock(x + 1, y, z).type === Material.NETHER_BRICKS
				val closedSouth = chunk.getBlock(x, y, z + 1).type === Material.NETHER_BRICKS
				val closedWest = chunk.getBlock(x - 1, y, z).type === Material.NETHER_BRICKS

				if (!closedNorth && !closedEast && !closedSouth && !closedWest) break

				val openFace = when {
					closedNorth && !closedSouth -> BlockFace.SOUTH
					closedEast && !closedWest -> BlockFace.WEST
					closedSouth && !closedNorth -> BlockFace.NORTH
					else -> BlockFace.EAST
				}

				for (cy in y + 2..y + 5) {
					if (chunk.getBlock(x, cy, z).type === Material.NETHER_BRICKS) return BlockPlacement(block, openFace)
				}
				break
			}
		}
		return null
	}
}

