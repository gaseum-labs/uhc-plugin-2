package org.gaseumlabs.uhcplugin.help

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.entity.Player
import org.gaseumlabs.uhcplugin.core.UHC
import org.gaseumlabs.uhcplugin.core.phase.PhaseType

interface ChatHelpRequirement

interface TickChatHelpRequirement : ChatHelpRequirement {
	fun check(player: Player): Boolean
}

class HasItemRequirement(val material: Material) : TickChatHelpRequirement {
	override fun check(player: Player): Boolean {
		return player.inventory.any { itemStack ->
			if (itemStack == null) return@any false
			itemStack.type === material
		}
	}
}

class HasAnyItemRequirement(vararg val materials: Material) : TickChatHelpRequirement {
	override fun check(player: Player): Boolean {
		return player.inventory.any { itemStack ->
			if (itemStack == null) return@any false
			materials.contains(itemStack.type)
		}
	}
}

class NearBlockRequirement(val material: Material, val radiusH: Int, val radiusV: Int) : TickChatHelpRequirement {
	override fun check(player: Player): Boolean {
		val block = player.location.block
		val world = block.world

		for (y in (block.y - radiusV).coerceAtLeast(-64)..(block.y + radiusV).coerceAtMost(319)) {
			for (x in (block.x - radiusH)..(block.x + radiusH)) {
				for (z in (block.z - radiusH)..(block.z + radiusH)) {
					val around = world.getBlockAt(x, y, z)
					if (around.type === material) return true
				}
			}
		}

		return false
	}
}

class BreakBlockRequirement(val material: Material) : ChatHelpRequirement

class YLevelRequirement(val range: IntRange) : TickChatHelpRequirement {
	override fun check(player: Player): Boolean {
		return player.location.block.y in range
	}
}

class InBiomeRequirement(val biome: Biome) : TickChatHelpRequirement {
	override fun check(player: Player): Boolean {
		return player.location.block.biome === biome
	}
}

class DimensionRequirement(val type: World.Environment) : TickChatHelpRequirement {
	override fun check(player: Player): Boolean {
		return player.world.environment === type
	}
}

class RadiusRangeRequirement(val radiusRange: IntRange) : TickChatHelpRequirement {
	override fun check(player: Player): Boolean {
		val block = player.location.block
		return block.x in radiusRange || block.z in radiusRange
	}
}

class AirRequirement(val airLevel: IntRange) : TickChatHelpRequirement {
	override fun check(player: Player): Boolean {
		return player.remainingAir in airLevel
	}
}

class GameTimeRequirement(val phaseType: PhaseType, val timeRemaining: Int) : TickChatHelpRequirement {
	override fun check(player: Player): Boolean {
		val phaseAlong = UHC.activeGame()?.getPhaseAlong() ?: return false
		if (phaseAlong.phase.type !== phaseType) return false

		return phaseAlong.duration - phaseAlong.timer <= timeRemaining
	}
}
