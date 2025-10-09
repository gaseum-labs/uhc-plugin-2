package org.gaseumlabs.uhcplugin

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import net.minecraft.core.*
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.features.VegetationFeatures.PATCH_MELON
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.util.valueproviders.UniformFloat
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
import net.minecraft.world.level.levelgen.carver.WorldCarver
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets
import net.minecraft.world.level.levelgen.structure.BuiltinStructures
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureSet
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.CraftServer
import java.util.*
import kotlin.jvm.optionals.getOrNull

object WorldGen {
	fun configureCarvers() {
		val server = (Bukkit.getServer() as CraftServer).server
		val registryAccess = server.registryAccess()

		val biomeRegistry = registryAccess.lookup(Registries.BIOME).get()
		val blockRegistry = registryAccess.lookup(Registries.BLOCK).get()
		val structureRegistry = registryAccess.lookup(Registries.STRUCTURE).get()
		val structureSetRegistry = registryAccess.lookup(Registries.STRUCTURE_SET).get()

		val uhcCaveCarver = createCarver(blockRegistry)

		for (biomeKey in overworldBiomesList) {
			val biome = biomeRegistry.getOrThrow(biomeKey).value()

			if (biomeKey === Biomes.JUNGLE) {
				val g = 4;
			}

			modifyBiome(biome, uhcCaveCarver)
		}

		modifyStructureSets(structureSetRegistry, structureRegistry)
	}

	fun modifyStructureSets(structureSetRegistry: Registry<StructureSet>, structureRegistry: Registry<Structure>) {
		val netherComplexesHolder = structureSetRegistry.getOrThrow(BuiltinStructureSets.NETHER_COMPLEXES)
		val netherComplexes = netherComplexesHolder.value()

		val fortress = structureRegistry.getOrThrow(BuiltinStructures.FORTRESS)

		val holderOwner = Reflection.getFieldValue<HolderOwner<StructureSet>>(netherComplexesHolder, "owner")
		val newStructureSet = StructureSet(fortress, CenterStructurePlacement())
		val newStructureSetHolder = Holder.Reference.createIntrusive<StructureSet>(holderOwner, newStructureSet)

		val registry = structureSetRegistry
		val mappedRegistry = registry as MappedRegistry<StructureSet>

		val byId = Reflection.getFieldValue<ObjectArrayList<Holder<StructureSet>>>(mappedRegistry, "byId")
		val toId = Reflection.getFieldValue<Reference2IntOpenHashMap<StructureSet>>(mappedRegistry, "toId")
		val byLocation =
			Reflection.getFieldValue<HashMap<ResourceLocation, Holder<StructureSet>>>(mappedRegistry, "byLocation")
		val byKey =
			Reflection.getFieldValue<HashMap<ResourceKey<StructureSet>, Holder<StructureSet>>>(mappedRegistry, "byKey")
		val byValue =
			Reflection.getFieldValue<IdentityHashMap<StructureSet, Holder<StructureSet>>>(mappedRegistry, "byValue")
		val registrationInfos = Reflection.getFieldValue<IdentityHashMap<ResourceKey<StructureSet>, RegistrationInfo>>(
			mappedRegistry,
			"registrationInfos"
		)

		val indexId = byId.indexOfFirst { structureSet -> structureSet.value() === netherComplexes }

		byId.set(indexId, newStructureSetHolder)
		toId.set(newStructureSet, indexId)
		byLocation.set(BuiltinStructureSets.NETHER_COMPLEXES.location(), newStructureSetHolder)
		byKey.set(BuiltinStructureSets.NETHER_COMPLEXES, newStructureSetHolder)
		byValue.remove(netherComplexes)
		byValue.set(newStructureSet, newStructureSetHolder)

		val g = 3;
	}

	fun modifyBiome(biome: Biome, uhcCaveCarver: ConfiguredWorldCarver<CaveCarverConfiguration>) {
		val carvers = biome.generationSettings.carvers

		val carversList = carvers.toMutableList()

		/* modify carvers */
		carversList.removeIf { carverHolder ->
			val carver = carverHolder.value()
			carver === WorldCarver.CAVE || carver === WorldCarver.NETHER_CAVE
		}
		carversList.add(Holder.Direct(uhcCaveCarver))

		Reflection.setFieldValue(carvers, "contents", carversList.toList())

		val features = biome.generationSettings.features()

		val featuresList = features.map { featureHolder ->
			val holders = featureHolder.toMutableList()

			val melonIndex = holders.indexOfFirst { holder ->
				holder.unwrapKey().getOrNull()?.location() == PATCH_MELON.location()
			}

			if (melonIndex != -1) {
				holders.removeAt(melonIndex)
			}

			if (melonIndex == -1) featureHolder else HolderSet.direct(holders)
		}

		Reflection.setFieldValue(biome.generationSettings, "features", featuresList)
	}

	fun createCarver(blockRegistry: Registry<Block>): ConfiguredWorldCarver<CaveCarverConfiguration> {
		return WorldCarver.CAVE.configured(
			CaveCarverConfiguration(
				1.0f,
				UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.Absolute(40)),
				UniformFloat.of(0.1f, 0.9f),
				VerticalAnchor.aboveBottom(16),
				CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()),
				blockRegistry.getOrThrow(BlockTags.OVERWORLD_CARVER_REPLACEABLES),
				UniformFloat.of(1.0f, 4.0f),
				UniformFloat.of(1.0f, 2.0f),
				UniformFloat.of(-1.0f, -0.25f)
			)
		)
	}
}

val biomesList = listOf(
	Biomes.THE_VOID,
	Biomes.PLAINS,
	Biomes.SUNFLOWER_PLAINS,
	Biomes.SNOWY_PLAINS,
	Biomes.ICE_SPIKES,
	Biomes.DESERT,
	Biomes.SWAMP,
	Biomes.MANGROVE_SWAMP,
	Biomes.FOREST,
	Biomes.FLOWER_FOREST,
	Biomes.BIRCH_FOREST,
	Biomes.DARK_FOREST,
	Biomes.PALE_GARDEN,
	Biomes.OLD_GROWTH_BIRCH_FOREST,
	Biomes.OLD_GROWTH_PINE_TAIGA,
	Biomes.OLD_GROWTH_SPRUCE_TAIGA,
	Biomes.TAIGA,
	Biomes.SNOWY_TAIGA,
	Biomes.SAVANNA,
	Biomes.SAVANNA_PLATEAU,
	Biomes.WINDSWEPT_HILLS,
	Biomes.WINDSWEPT_GRAVELLY_HILLS,
	Biomes.WINDSWEPT_FOREST,
	Biomes.WINDSWEPT_SAVANNA,
	Biomes.JUNGLE,
	Biomes.SPARSE_JUNGLE,
	Biomes.BAMBOO_JUNGLE,
	Biomes.BADLANDS,
	Biomes.ERODED_BADLANDS,
	Biomes.WOODED_BADLANDS,
	Biomes.MEADOW,
	Biomes.CHERRY_GROVE,
	Biomes.GROVE,
	Biomes.SNOWY_SLOPES,
	Biomes.FROZEN_PEAKS,
	Biomes.JAGGED_PEAKS,
	Biomes.STONY_PEAKS,
	Biomes.RIVER,
	Biomes.FROZEN_RIVER,
	Biomes.BEACH,
	Biomes.SNOWY_BEACH,
	Biomes.STONY_SHORE,
	Biomes.WARM_OCEAN,
	Biomes.LUKEWARM_OCEAN,
	Biomes.DEEP_LUKEWARM_OCEAN,
	Biomes.OCEAN,
	Biomes.DEEP_OCEAN,
	Biomes.COLD_OCEAN,
	Biomes.DEEP_COLD_OCEAN,
	Biomes.FROZEN_OCEAN,
	Biomes.DEEP_FROZEN_OCEAN,
	Biomes.MUSHROOM_FIELDS,
	Biomes.DRIPSTONE_CAVES,
	Biomes.LUSH_CAVES,
	Biomes.DEEP_DARK,
	Biomes.NETHER_WASTES,
	Biomes.WARPED_FOREST,
	Biomes.CRIMSON_FOREST,
	Biomes.SOUL_SAND_VALLEY,
	Biomes.BASALT_DELTAS,
	Biomes.THE_END,
	Biomes.END_HIGHLANDS,
	Biomes.END_MIDLANDS,
	Biomes.SMALL_END_ISLANDS,
	Biomes.END_BARRENS,
)

val overworldBiomesList = listOf(
	Biomes.PLAINS,
	Biomes.SUNFLOWER_PLAINS,
	Biomes.SNOWY_PLAINS,
	Biomes.ICE_SPIKES,
	Biomes.DESERT,
	Biomes.SWAMP,
	Biomes.MANGROVE_SWAMP,
	Biomes.FOREST,
	Biomes.FLOWER_FOREST,
	Biomes.BIRCH_FOREST,
	Biomes.DARK_FOREST,
	Biomes.PALE_GARDEN,
	Biomes.OLD_GROWTH_BIRCH_FOREST,
	Biomes.OLD_GROWTH_PINE_TAIGA,
	Biomes.OLD_GROWTH_SPRUCE_TAIGA,
	Biomes.TAIGA,
	Biomes.SNOWY_TAIGA,
	Biomes.SAVANNA,
	Biomes.SAVANNA_PLATEAU,
	Biomes.WINDSWEPT_HILLS,
	Biomes.WINDSWEPT_GRAVELLY_HILLS,
	Biomes.WINDSWEPT_FOREST,
	Biomes.WINDSWEPT_SAVANNA,
	Biomes.JUNGLE,
	Biomes.SPARSE_JUNGLE,
	Biomes.BAMBOO_JUNGLE,
	Biomes.BADLANDS,
	Biomes.ERODED_BADLANDS,
	Biomes.WOODED_BADLANDS,
	Biomes.MEADOW,
	Biomes.CHERRY_GROVE,
	Biomes.GROVE,
	Biomes.SNOWY_SLOPES,
	Biomes.FROZEN_PEAKS,
	Biomes.JAGGED_PEAKS,
	Biomes.STONY_PEAKS,
	Biomes.RIVER,
	Biomes.FROZEN_RIVER,
	Biomes.BEACH,
	Biomes.SNOWY_BEACH,
	Biomes.STONY_SHORE,
	Biomes.WARM_OCEAN,
	Biomes.LUKEWARM_OCEAN,
	Biomes.DEEP_LUKEWARM_OCEAN,
	Biomes.OCEAN,
	Biomes.DEEP_OCEAN,
	Biomes.COLD_OCEAN,
	Biomes.DEEP_COLD_OCEAN,
	Biomes.FROZEN_OCEAN,
	Biomes.DEEP_FROZEN_OCEAN,
	Biomes.MUSHROOM_FIELDS,
	Biomes.DRIPSTONE_CAVES,
	Biomes.LUSH_CAVES,
	Biomes.DEEP_DARK,
)
