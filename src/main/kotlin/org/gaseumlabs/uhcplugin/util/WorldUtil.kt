package org.gaseumlabs.uhcplugin.util

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.levelgen.structure.BuiltinStructures
import net.minecraft.world.level.levelgen.structure.StructureStart
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.craftbukkit.CraftWorld

object WorldUtil {
	fun isFortressAt(world: World, x: Int, y: Int, z: Int): Boolean {
		val craftWorld = world as CraftWorld
		val structureManager = craftWorld.handle.structureManager()

		val registry = craftWorld.handle.registryAccess().lookupOrThrow(Registries.STRUCTURE)
		val fortress = registry.getValueOrThrow(BuiltinStructures.FORTRESS)

		val structureStart = structureManager.getStructureAt(BlockPos(x, y, z), fortress)

		return structureStart !== StructureStart.INVALID_START
	}

	fun blocksEqual(a: Block, b: Block): Boolean {
		return a.world === b.world && a.x == b.x && a.y == b.y && a.z == b.z
	}
}
