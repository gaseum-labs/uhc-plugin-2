package org.gaseumlabs.uhcplugin

import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.*
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator
import net.minecraft.world.level.levelgen.NoiseRouter
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.event.world.WorldLoadEvent
import java.util.stream.Stream
import kotlin.random.Random

object Experimental {
	fun onWorldPrepare(event: WorldLoadEvent) {
		if (event.world.name == "world") {
			val serverLevel = (event.world as CraftWorld).handle

			val registryAccess = serverLevel.registryAccess()
			val biomeRegistry = registryAccess.lookup(Registries.BIOME).get()

			val jungleRef = biomeRegistry.getOrThrow(Biomes.JUNGLE)

			val biomeList = arrayOf(
				biomeRegistry.getOrThrow(Biomes.JUNGLE),
				biomeRegistry.getOrThrow(Biomes.OCEAN),
				biomeRegistry.getOrThrow(Biomes.RIVER),
				biomeRegistry.getOrThrow(Biomes.PLAINS),
				biomeRegistry.getOrThrow(Biomes.ICE_SPIKES),
				biomeRegistry.getOrThrow(Biomes.OLD_GROWTH_PINE_TAIGA),
			)

			val biomeSet = HolderSet.direct(*biomeList)

			val customNoiseBiomeSource = BiomeManager.NoiseBiomeSource { x, y, z ->
				val chunkX = x / 4
				val chunkZ = z / 4

				biomeList[Random(chunkX.toLong().and(0x0000ffff).or(chunkZ.toLong().and(0x0000ffff).shl(32))).nextInt(
					biomeList.size
				)]
			}

			val customBiomeSource = object : CheckerboardColumnBiomeSource(biomeSet, 1) {
				override fun collectPossibleBiomes(): Stream<Holder<Biome>> {
					return biomeSet.stream()
				}

				override fun getNoiseBiome(
					x: Int,
					y: Int,
					z: Int,
					climateSampler: Climate.Sampler,
				): Holder<Biome> {
					val chunkX = x / 4
					val chunkZ = z / 4

					return biomeList[Random(
						chunkX.toLong().and(0x0000ffff).or(chunkZ.toLong().and(0x0000ffff).shl(32))
					).nextInt(biomeList.size)]
				}

			}

			val newBiomeManager = serverLevel.biomeManager.withDifferentSource(customNoiseBiomeSource)

			val chunkGenerator = serverLevel.chunkSource.generator

			Reflection.setFieldValue(serverLevel, Level::class.java, "biomeManager", newBiomeManager)
			Reflection.setFieldValue(chunkGenerator, ChunkGenerator::class.java, "biomeSource", customBiomeSource)

			val noiseBasedChunkGenerator = chunkGenerator as NoiseBasedChunkGenerator

			val randomState = serverLevel.chunkSource.randomState()

			val oldNoiseRouter = randomState.router()

			val densityFunctionRegistry = registryAccess.lookupOrThrow(Registries.DENSITY_FUNCTION)
			val noiseRegistry = registryAccess.lookupOrThrow(Registries.NOISE)

			val finalDensity =
				UHCNoiseRouterData.createOverworldFinalDensity(densityFunctionRegistry, noiseRegistry, false)

			//val newNoiseRouter = NoiseRouter(
			//	oldNoiseRouter.barrierNoise(),
			//	oldNoiseRouter.fluidLevelFloodednessNoise(),
			//	oldNoiseRouter.fluidLevelSpreadNoise(),
			//	oldNoiseRouter.lavaNoise(),
			//	oldNoiseRouter.temperature(),
			//	oldNoiseRouter.vegetation(),
			//	newContinentsFunction,
			//	oldNoiseRouter.erosion(),
			//	newDepthFunction,
			//	oldNoiseRouter.ridges(),
			//	oldNoiseRouter.initialDensityWithoutJaggedness(),
			//	oldNoiseRouter.finalDensity(),
			//	oldNoiseRouter.veinToggle(),
			//	oldNoiseRouter.veinRidged(),
			//	oldNoiseRouter.veinGap()
			//)

			val newNoiseRouter = NoiseRouter(
				oldNoiseRouter.barrierNoise,
				oldNoiseRouter.fluidLevelFloodednessNoise,
				oldNoiseRouter.fluidLevelSpreadNoise,
				oldNoiseRouter.lavaNoise,
				oldNoiseRouter.temperature,
				oldNoiseRouter.vegetation,
				oldNoiseRouter.continents,
				oldNoiseRouter.erosion,
				oldNoiseRouter.depth,
				oldNoiseRouter.ridges,
				oldNoiseRouter.initialDensityWithoutJaggedness,
				finalDensity,
				oldNoiseRouter.veinToggle,
				oldNoiseRouter.veinRidged,
				oldNoiseRouter.veinGap
			)

			//offset, factor, jaggedness, depth, sloped_cheese

			//NoiseData | for continentalness
			//NoiseRouterData | for overworld
			//

			Reflection.setFieldValue(randomState, "router", newNoiseRouter)
		}
	}
}