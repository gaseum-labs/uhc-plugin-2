package org.gaseumlabs.uhcplugin.help

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.gaseumlabs.uhcplugin.core.phase.PhaseType
import org.gaseumlabs.uhcplugin.core.timer.TickTime

enum class ChatHelpItem(val message: String, val requirements: List<ChatHelpRequirement>) {
	FIND_MELON(
		"Melons generate in all temperate forest biomes", listOf(
			NearBlockRequirement(Material.MELON, 5, 1)
		)
	),
	BUILD_PORTAL(
		"You can use Lava and Water Buckets to build a Nether Portal", listOf(
			HasItemRequirement(Material.WATER_BUCKET),
			NearBlockRequirement(Material.LAVA, 5, 1)
		)
	),
	FIND_SAND(
		"Collect Sand to craft Glass Bottles for brewing", listOf(
			NearBlockRequirement(Material.SAND, 5, 2)
		)
	),
	CAN_CRAFT_BUNDLE(
		"You can use one String and one Leather to craft a Bundle", listOf(
			HasItemRequirement(Material.LEATHER),
			HasItemRequirement(Material.STRING),
		)
	),
	MINE_LEAVES(
		"Hoes mine Leaves faster and can drop Apples, while Shears give you leaf blocks", listOf(
			BreakBlockRequirement(Material.OAK_LEAVES)
		)
	),
	HAS_MOST_OVERWORLD(
		"It's a good idea to mine down to find Iron", listOf(
			HasItemRequirement(Material.LEATHER),
			HasItemRequirement(Material.SUGAR_CANE),
			HasItemRequirement(Material.APPLE),
			HasItemRequirement(Material.STONE_PICKAXE),
			HasAnyItemRequirement(Material.BEEF, Material.CHICKEN, Material.PORKCHOP, Material.MUTTON),
			HasAnyItemRequirement(Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG, Material.JUNGLE_LOG),
			YLevelRequirement(63..319)
		)
	),
	CAN_CRAFT_SHIELD(
		"Shields protect you from mobs and players alike", listOf(
			HasAnyItemRequirement(Material.OAK_PLANKS, Material.BIRCH_PLANKS, Material.SPRUCE_PLANKS, Material.JUNGLE_PLANKS),
			HasItemRequirement(Material.IRON_INGOT),
		)
	),
	OUTER_RING_NETHER(
		"Head to the center of the Nether to find the Fortress", listOf(
			DimensionRequirement(World.Environment.NETHER),
			RadiusRangeRequirement(128..1000)
		)
	),
	IN_PLAINS(
		"Oxeye Daisies generate in Plains biomes and can be turned into Suspicious Stews for healing", listOf(
			InBiomeRequirement(Biome.PLAINS)
		)
	),
	UNDERWATER(
		"Craft doors to create a breathable air pocket underwater", listOf(
			AirRequirement(0..TickTime.ofSeconds(10))
		)
	),
	GRACE_END(
		"Eat food now to fully heal before regeneration is turned off", listOf(
			GameTimeRequirement(PhaseType.GRACE, TickTime.ofSeconds(30))
		)
	),
	SHARE_COORDS(
		"Use /sharecoords to save your location or share it with your team", listOf(
			GameTimeRequirement(PhaseType.GRACE, TickTime.ofMinutes(15))
		)
	)
}
