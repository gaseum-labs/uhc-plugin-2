package org.gaseumlabs.uhcplugin

import net.minecraft.core.Holder
import net.minecraft.core.HolderGetter
import net.minecraft.core.Registry
import net.minecraft.data.worldgen.TerrainProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.DensityFunction
import net.minecraft.world.level.levelgen.DensityFunctions
import net.minecraft.world.level.levelgen.DensityFunctions.HolderHolder
import net.minecraft.world.level.levelgen.DensityFunctions.Spline
import net.minecraft.world.level.levelgen.Noises
import net.minecraft.world.level.levelgen.synth.BlendedNoise
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters

object UHCNoiseRouterData {
	fun createBaseSlopedCheese(noiseRegistry: Registry<NoiseParameters>, noOcean: Boolean): DensityFunction {
		val shiftX = DensityFunctions.flatCache(
				DensityFunctions.cache2d(
					DensityFunctions.shiftA(
						noiseRegistry.getOrThrow(Noises.SHIFT)
					)
				)
			)

		val shiftZ =
			DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(noiseRegistry.getOrThrow(Noises.SHIFT))))

		val base3dNoiseOverworld = BlendedNoise.createUnseeded(0.25, 0.125, 80.0, 160.0, 8.0)

		val continents = if (noOcean) DensityFunctions.constant(1.0) else DensityFunctions.flatCache(
			DensityFunctions.shiftedNoise2d(
				shiftX,
				shiftZ,
				0.25,
				noiseRegistry.getOrThrow(Noises.CONTINENTALNESS)
			)
		)

		val erosion = DensityFunctions.flatCache(
				DensityFunctions.shiftedNoise2d(
					shiftX,
					shiftZ,
					0.25,
					noiseRegistry.getOrThrow(Noises.EROSION)
				)
			)

		val ridges =
			DensityFunctions.flatCache(
				DensityFunctions.shiftedNoise2d(
					shiftX,
					shiftZ,
					0.25,
					noiseRegistry.getOrThrow(Noises.RIDGE)
				)
			)

		val ridgesFolded = peaksAndValleys(ridges)

		val densityFunction3 = DensityFunctions.noise(noiseRegistry.getOrThrow(Noises.JAGGED), 1500.0, 0.0)

		return createSlopedCheese(
			Holder.Direct(ridges),
			Holder.Direct(ridgesFolded),
			densityFunction3,
			Holder.Direct(continents),
			Holder.Direct(erosion),
			false,
			base3dNoiseOverworld,
		)
	}

	fun peaksAndValleys(densityFunction: DensityFunction): DensityFunction {
		return DensityFunctions.mul(
			DensityFunctions.add(
				DensityFunctions.add(densityFunction.abs(), DensityFunctions.constant(-0.6666666666666666)).abs(),
				DensityFunctions.constant(-0.3333333333333333)
			),
			DensityFunctions.constant(-3.0)
		)
	}

	fun createSlopedCheese(
		ridges: Holder<DensityFunction>,
		ridgesFolded: Holder<DensityFunction>,
		jaggedNoise: DensityFunction,
		continentalness: Holder<DensityFunction>,
		erosion: Holder<DensityFunction>,
		amplified: Boolean,
		base3dNoiseOverworld: DensityFunction
	): DensityFunction {
		val coordinate = Spline.Coordinate(continentalness)
		val coordinate1 = Spline.Coordinate(erosion)
		val coordinate2 = Spline.Coordinate(ridges)
		val coordinate3 = Spline.Coordinate(ridgesFolded)

		val densityFunction = splineWithBlending(
			DensityFunctions.add(
				DensityFunctions.constant(-0.50375),
				DensityFunctions.spline(
					TerrainProvider.overworldOffset(
						coordinate,
						coordinate1,
						coordinate3,
						amplified
					)
				)
			),
			DensityFunctions.blendOffset()
		)

		val blendingFactor = DensityFunctions.constant(10.0)

		val densityFunction1 = splineWithBlending(
			DensityFunctions.spline(
				TerrainProvider.overworldFactor(
					coordinate,
					coordinate1,
					coordinate2,
					coordinate3,
					amplified
				)
			), blendingFactor
		)

		val densityFunction2 = DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5), densityFunction)

		val blendingJaggedness = DensityFunctions.zero()

		val densityFunction3 = splineWithBlending(
				DensityFunctions.spline(
					TerrainProvider.overworldJaggedness(
						coordinate,
						coordinate1,
						coordinate2,
						coordinate3,
						amplified
					)
				), blendingJaggedness
			)

		val densityFunction4 = DensityFunctions.mul(densityFunction3, jaggedNoise.halfNegative())

		val densityFunction5 = noiseGradientDensity(
			densityFunction1,
			DensityFunctions.add(densityFunction2, densityFunction4)
		)

		val slopedCheese = DensityFunctions.add(
			densityFunction5,
			base3dNoiseOverworld
		)

		return slopedCheese
	}

	fun noiseGradientDensity(minFunction: DensityFunction, maxFunction: DensityFunction): DensityFunction {
		val densityFunction = DensityFunctions.mul(maxFunction, minFunction)
		return DensityFunctions.mul(DensityFunctions.constant(4.0), densityFunction.quarterNegative())
	}

	fun splineWithBlending(minFunction: DensityFunction, maxFunction: DensityFunction): DensityFunction {
		val densityFunction = DensityFunctions.lerp(DensityFunctions.blendAlpha(), maxFunction, minFunction)
		return DensityFunctions.flatCache(DensityFunctions.cache2d(densityFunction))
	}

	fun createOverworldFinalDensity(densityFunctionRegistry: Registry<DensityFunction>, noiseFunctionRegistry: Registry<NoiseParameters>, noOcean: Boolean): DensityFunction {
		val function4 = createBaseSlopedCheese(noiseFunctionRegistry, noOcean)

		//TODO actually recreate the entire noise router in here instead of just final density...

		//val densityFunction7 = DensityFunctions.min(
		//	function4,
		//	DensityFunctions.mul(
		//		DensityFunctions.constant(5.0),
		//		getFunction(densityFunctionRegistry, DensityFunctionKeys.ENTRANCES)
		//	)
		//)
//
		//val densityFunction8 = DensityFunctions.rangeChoice(
		//	function4,
		//	-1000000.0,
		//	1.5625,
		//	densityFunction7,
		//	createUndergroundDensity()
		//)

		//val densityFunction9 = DensityFunctions.min(
		//	postProcess(slideOverworld(false,function8)),
		//	getFunction(densityFunctionRegistry, DensityFunctionKeys.NOODLE)
		//)

		return postProcess(slideOverworld(false,function4))
	}

	fun createUndergroundDensity(): DensityFunction {
		return DensityFunctions.constant(-10.0)
	}

	fun getFunction(
		densityFunctionRegistry: HolderGetter<DensityFunction>,
		key: ResourceKey<DensityFunction>
	): DensityFunction {
		return HolderHolder(densityFunctionRegistry.getOrThrow(key))
	}

	fun postProcess(densityFunction: DensityFunction): DensityFunction {
		val densityFunction1 = DensityFunctions.blendDensity(densityFunction)
		return DensityFunctions.mul(DensityFunctions.interpolated(densityFunction1), DensityFunctions.constant(0.64))
			.squeeze()
	}

	fun slideOverworld(amplified: Boolean, densityFunction: DensityFunction): DensityFunction {
		return slide(
			densityFunction,
			-64,
			384,
			if (amplified) 16 else 80,
			if (amplified) 0 else 64,
			-0.078125,
			0,
			24,
			if (amplified) 0.4 else 0.1171875
		)
	}

	fun slide(
		input: DensityFunction,
		minY: Int,
		height: Int,
		topStartOffset: Int,
		topEndOffset: Int,
		topDelta: Double,
		bottomStartOffset: Int,
		bottomEndOffset: Int,
		bottomDelta: Double
	): DensityFunction {
		val densityFunction1 =
			DensityFunctions.yClampedGradient(minY + height - topStartOffset, minY + height - topEndOffset, 1.0, 0.0)
		val densityFunction = DensityFunctions.lerp(densityFunction1, topDelta, input)
		val densityFunction2 =
			DensityFunctions.yClampedGradient(minY + bottomStartOffset, minY + bottomEndOffset, 0.0, 1.0)
		return DensityFunctions.lerp(densityFunction2, bottomDelta, densityFunction)
	}
}