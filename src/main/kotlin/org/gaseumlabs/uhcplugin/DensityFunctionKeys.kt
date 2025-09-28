package org.gaseumlabs.uhcplugin

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.DensityFunction

object DensityFunctionKeys {
	val ZERO = createKey("zero");
	val Y = createKey("y");
	val SHIFT_X = createKey("shift_x");
	val SHIFT_Z = createKey("shift_z");
	val BASE_3D_NOISE_OVERWORLD = createKey("overworld/base_3d_noise");
	val BASE_3D_NOISE_NETHER = createKey("nether/base_3d_noise");
	val BASE_3D_NOISE_END = createKey("end/base_3d_noise");
	val CONTINENTS = createKey("overworld/continents");
	val EROSION = createKey("overworld/erosion");
	val RIDGES = createKey("overworld/ridges");
	val RIDGES_FOLDED = createKey("overworld/ridges_folded");
	val OFFSET = createKey("overworld/offset");
	val FACTOR = createKey("overworld/factor");
	val JAGGEDNESS = createKey("overworld/jaggedness");
	val DEPTH = createKey("overworld/depth");
	val SLOPED_CHEESE = createKey("overworld/sloped_cheese");
	val CONTINENTS_LARGE = createKey("overworld_large_biomes/continents");
	val EROSION_LARGE = createKey("overworld_large_biomes/erosion");
	val OFFSET_LARGE = createKey("overworld_large_biomes/offset");
	val FACTOR_LARGE = createKey("overworld_large_biomes/factor");
	val JAGGEDNESS_LARGE = createKey("overworld_large_biomes/jaggedness");
	val DEPTH_LARGE = createKey("overworld_large_biomes/depth");
	val SLOPED_CHEESE_LARGE = createKey("overworld_large_biomes/sloped_cheese");
	val OFFSET_AMPLIFIED = createKey("overworld_amplified/offset");
	val FACTOR_AMPLIFIED = createKey("overworld_amplified/factor");
	val JAGGEDNESS_AMPLIFIED = createKey("overworld_amplified/jaggedness");
	val DEPTH_AMPLIFIED = createKey("overworld_amplified/depth");
	val SLOPED_CHEESE_AMPLIFIED = createKey("overworld_amplified/sloped_cheese");
	val SLOPED_CHEESE_END = createKey("end/sloped_cheese");
	val SPAGHETTI_ROUGHNESS_FUNCTION = createKey("overworld/caves/spaghetti_roughness_function");
	val ENTRANCES = createKey("overworld/caves/entrances");
	val NOODLE = createKey("overworld/caves/noodle");
	val PILLARS = createKey("overworld/caves/pillars");
	val SPAGHETTI_2D_THICKNESS_MODULATOR = createKey("overworld/caves/spaghetti_2d_thickness_modulator");
	val SPAGHETTI_2D = createKey("overworld/caves/spaghetti_2d");

	fun createKey(location: String): ResourceKey<DensityFunction> {
		return ResourceKey.create(Registries.DENSITY_FUNCTION, ResourceLocation.withDefaultNamespace(location))
	}
}
