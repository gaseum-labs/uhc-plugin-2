package org.gaseumlabs.uhcplugin.lootRegen

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.levelgen.structure.BuiltinStructures
import net.minecraft.world.level.levelgen.structure.StructureStart
import org.bukkit.Chunk
import org.bukkit.block.Block
import org.bukkit.craftbukkit.CraftWorld
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import org.gaseumlabs.uhcplugin.core.timer.TickTime
import java.util.*

class LootRegen {
	data class Loot(val chunk: Chunk, val chest: Block, val playerUuid: UUID, val time: Int)

	data class TeamLoot(var numFound: Int)

	val MAX_FOUND_PER_TEAM = 4
	val MAX_LOOT_TIME = TickTime.ofMinutes(2)

	val loots = ArrayList<Loot>()
	val teamLoots = HashMap<UUID, TeamLoot>()

	fun tick(activeGame: ActiveGame) {
		val netherWorld = activeGame.netherWorld as CraftWorld
		val structureManager = netherWorld.handle.structureManager()

		val registry = netherWorld.handle.registryAccess().getOrThrow(Registries.STRUCTURE).value()
		val fortress = registry.getValueOrThrow(BuiltinStructures.FORTRESS)

		val structureStart = structureManager.getStructureAt(BlockPos(1, 2, 3), fortress)

		if (structureStart === StructureStart.INVALID_START) {

		}

		//val fortressTeams = activeGame.teams.teams.filter { team ->
		//	team.members.any { playerData ->
		//		val player = playerData.getPlayer() ?: return@any false
		//		if (player.world !== activeGame.netherWorld) return@any false
		//		val world = player.world as CraftWorld
		//	}
		//}
	}
}
