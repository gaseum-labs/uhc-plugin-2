package org.gaseumlabs.uhcplugin.help

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome

enum class ChatHelpItem(val message: String, val requirements: List<ChatHelpRequirement>) {
	FIND_MELON(
		"Melons generate in all temperate forested biomes", listOf(
			NearBlockRequirement(Material.MELON, 5, 1)
		)
	),
	BUILD_PORTAL(
		"use lava and water buckets to construct a portal to the nether", listOf(
			HasItemRequirement(Material.WATER_BUCKET),
			NearBlockRequirement(Material.LAVA, 5, 1)
		)
	),
	FIND_SAND(
		"Remember to collect sand to craft bottles", listOf(
			NearBlockRequirement(Material.SAND, 2, 2)
		)
	),
	CAN_CRAFT_BUNDLE(
		"You can use one string and one leather to craft a bundle", listOf(
			HasItemRequirement(Material.LEATHER),
			HasItemRequirement(Material.STRING),
		)
	),
	MINE_LEAVES(
		"Hoes mine leaves faster for apples. Shears give you leaf blocks", listOf(
			BreakBlockRequirement(Material.OAK_LEAVES)
		)
	),
	HAS_MOST_OVERWORLD(
		"It's a good idea to mine down to find iron", listOf(
			HasItemRequirement(Material.LEATHER),
			HasItemRequirement(Material.SUGAR_CANE),
			HasItemRequirement(Material.APPLE),
			HasItemRequirement(Material.STONE_PICKAXE),
			HasAnyItemRequirement(Material.BEEF, Material.CHICKEN, Material.PORKCHOP, Material.MUTTON),
			HasAnyItemRequirement(Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG, Material.JUNGLE_LOG),
			YLevelRequirement(63..319)
		)
	),
	OUTER_RING_NETHER(
		"Go to the center of the nether to find the fortress", listOf(
			DimensionRequirement(World.Environment.NETHER),
			RadiusRangeRequirement(128..1000)
		)
	),
	HAS_BLAZE_ROD(
		"Nether wart generates in patches of 1 in all nether biomes", listOf(
			DimensionRequirement(World.Environment.NETHER),
			HasItemRequirement(Material.BLAZE_ROD)
		)
	),
	IN_PLAINS(
		"Oxeye daisies generate in plains biomes. They can be used to craft regeneration suspicious stew", listOf(
			InBiomeRequirement(Biome.PLAINS)
		)
	)
}
