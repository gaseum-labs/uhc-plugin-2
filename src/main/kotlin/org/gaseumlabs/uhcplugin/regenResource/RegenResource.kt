package org.gaseumlabs.uhcplugin.regenResource

import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.block.Block

abstract class RegenResource(
	val environment: World.Environment,
	val xRange: IntRange,
	val zRange: IntRange,
	val radius: Int,
	val generateChance: Double,
) {
	abstract fun setBlock(chunk: Chunk): Block?

	abstract fun isUntouched(block: Block): Boolean
}