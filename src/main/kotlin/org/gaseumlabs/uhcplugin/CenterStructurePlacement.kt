package org.gaseumlabs.uhcplugin

import net.minecraft.core.Vec3i
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState
import net.minecraft.world.level.levelgen.structure.StructureSet
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType
import java.util.Optional
import kotlin.math.absoluteValue

class CenterStructurePlacement : StructurePlacement(
	Vec3i(0, 0, 0),
	FrequencyReductionMethod.DEFAULT,
	1.0f,
	1,
	Optional.empty()
) {
	override fun isPlacementChunk(
		structureState: ChunkGeneratorStructureState,
		x: Int,
		z: Int
	): Boolean {
		return x.absoluteValue <= 1 && z.absoluteValue <= 1
	}

	override fun isStructureChunk(
		structureState: ChunkGeneratorStructureState,
		x: Int,
		z: Int,
		structureSetKey: ResourceKey<StructureSet?>?
	): Boolean {
		return isPlacementChunk(structureState, x, z)
	}

	override fun type(): StructurePlacementType<*> {
		return StructurePlacementType.CONCENTRIC_RINGS
	}
}
