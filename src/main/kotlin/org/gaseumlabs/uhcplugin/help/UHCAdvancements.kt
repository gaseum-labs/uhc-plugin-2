package org.gaseumlabs.uhcplugin.help

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemType
import org.bukkit.potion.PotionType
import org.gaseumlabs.uhcplugin.UHCPlugin

object UHCAdvancements {
	val UHC = UHCAdvancement.createRoot(
		key = UHCPlugin.key("uhc"),
		icon = ItemType.GOLDEN_AXE.createItemStack(),
		title = Component.text("UHC"),
		description = Component.text("GL, HF!"),
		background = "minecraft:block/dirt",
		criteria = UHCAdvancement.Criteria.impossible()
	)

	val UHC_WOOD = UHC.append(
		key = UHCPlugin.key("uhc_wood"),
		icon = ItemType.OAK_LOG.createItemStack(),
		title = Component.text("Getting Wood"),
		description = Component.text("Half a stack should be plenty!"),
		criteria = UHCAdvancement.Criteria.obtainTag("#minecraft:logs")
	)

	val UHC_APPLE = UHC_WOOD.append(
		key = UHCPlugin.key("uhc_apple"),
		icon = ItemType.APPLE.createItemStack(6),
		title = Component.text("6 Apples"),
		description = Component.text("Dropped from every type of leaves in the game!"),
		criteria = UHCAdvancement.Criteria.obtainItemList(Material.APPLE)
	)

	val UHC_FOOD = UHC_WOOD.append(
		key = UHCPlugin.key("uhc_food"),
		icon = ItemType.COOKED_BEEF.createItemStack(),
		title = Component.text("Does Done Cooking"),
		description = Component.text("You'll need food to sprint! A dozen steak will do just fine."),
		criteria = UHCAdvancement.Criteria.obtainItemList(
			Material.BEEF, Material.CHICKEN, Material.PORKCHOP, Material.MUTTON
		)
	)

	val UHC_LEATHER = UHC_FOOD.append(
		key = UHCPlugin.key("uhc_leather"),
		icon = ItemType.LEATHER.createItemStack(),
		title = Component.text("Hardly Know 'Er"),
		description = Component.text("A necessity for books. More can't hurt!"),
		criteria = UHCAdvancement.Criteria.obtainItemList(Material.LEATHER)
	)

	val UHC_CANE = UHC_WOOD.append(
		key = UHCPlugin.key("uhc_cane"),
		icon = ItemType.SUGAR_CANE.createItemStack(),
		title = Component.text("And Everything Nice"),
		description = Component.text("Books and brewing. Can't have too much!"),
		criteria = UHCAdvancement.Criteria.obtainItemList(Material.SUGAR_CANE)
	)

	val UHC_SAND = UHC_CANE.append(
		key = UHCPlugin.key("uhc_sand"),
		icon = ItemType.SAND.createItemStack(),
		title = Component.text("Don't Forget!"),
		description = Component.text("Half a stack should do. You'll need Glass Bottles!"),
		criteria = UHCAdvancement.Criteria.obtainItemList(Material.SAND)
	)

	val UHC_MELON = UHC_WOOD.append(
		key = UHCPlugin.key("uhc_melon"),
		icon = ItemType.MELON_SLICE.createItemStack(),
		title = Component.text("Pink Pill"),
		description = Component.text("Used for instant health potions. Find a jungle and check the bushes!"),
		criteria = UHCAdvancement.Criteria.obtainItemList(Material.MELON_SLICE)
	)

	val UHC_IRON = UHC_SAND.append(
		key = UHCPlugin.key("uhc_iron"),
		icon = ItemType.RAW_IRON.createItemStack(),
		title = Component.text("Acquire Hardware"),
		description = Component.text("Used for just about everything. Grab it if you see it!"),
		criteria = UHCAdvancement.Criteria.obtainItemList(Material.RAW_IRON)
	)

	val UHC_GOLD = UHC_IRON.append(
		key = UHCPlugin.key("uhc_gold"),
		icon = ItemType.RAW_GOLD.createItemStack(),
		title = Component.text("Acquire Hardware (Software is funny too)"),
		description = Component.text("Used for Golden Applies and Glistering Melon. Grab as much as it takes!"),
		criteria = UHCAdvancement.Criteria.obtainItemList(Material.RAW_GOLD)
	)

	val UHC_GLISTERING_MELON = UHC_GOLD.append(
		key = UHCPlugin.key("uhc_glistering_melon"),
		icon = ItemType.GLISTERING_MELON_SLICE.createItemStack(),
		title = Component.text("Glistering Melon"),
		description = Component.text("Used for instant health potions. Combine melon slices and gold."),
		criteria = UHCAdvancement.Criteria.craftItem(Material.GLISTERING_MELON_SLICE)
	)

	val UHC_GOLDEN_APPLE = UHC_GOLD.append(
		key = UHCPlugin.key("uhc_golden_apple"),
		icon = ItemType.GOLDEN_APPLE.createItemStack(),
		title = Component.text("Acquire Hardware (Software is funny too)"),
		description = Component.text("Used for Golden Applies and Glistering Melon. Grab as much as it takes!"),
		criteria = UHCAdvancement.Criteria.craftItem(Material.GOLDEN_APPLE)
	)

	val UHC_DIAMOND = UHC_GOLD.append(
		key = UHCPlugin.key("uhc_diamond"),
		icon = ItemType.DIAMOND.createItemStack(),
		title = Component.text("Diamonds!"),
		description = Component.text("Used for armor and weapons. The more the merrier!"),
		criteria = UHCAdvancement.Criteria.obtainItemList(Material.DIAMOND)
	)

	val UHC_OBSIDIAN = UHC_DIAMOND.append(
		key = UHCPlugin.key("uhc_obsidian"),
		icon = ItemType.OBSIDIAN.createItemStack(),
		title = Component.text("Ice Bucket Challenge"),
		description = Component.text("Nice for enchanting, nice for Nethering"),
		criteria = UHCAdvancement.Criteria.obtainItemList(Material.OBSIDIAN)
	)

	val UHC_ENCHANTING_TABLE = UHC_OBSIDIAN.append(
		key = UHCPlugin.key("uhc_enchanting_table"),
		icon = ItemType.ENCHANTING_TABLE.createItemStack(),
		title = Component.text("Enchanting Table"),
		description = Component.text("Craft an enchanting table."),
		criteria = UHCAdvancement.Criteria.obtainItemList(Material.ENCHANTING_TABLE)
	)

	val UHC_SHARP_AXE = UHC_ENCHANTING_TABLE.append(
		key = UHCPlugin.key("uhc_sharp_axe"),
		icon = ItemType.DIAMOND_AXE.createItemStack { meta -> meta.addEnchant(Enchantment.SHARPNESS, 1, true) },
		title = Component.text("Sharp Axe"),
		description = Component.text("Use an enchanted book and anvil to put sharpness on an axe"),
		criteria = UHCAdvancement.Criteria.obtainItem(ItemType.DIAMOND_AXE.createItemStack { meta ->
			meta.addEnchant(
				Enchantment.SHARPNESS,
				1,
				true
			)
		})
	)

	val UHC_NETHER = UHC_OBSIDIAN.append(
		key = UHCPlugin.key("uhc_nether"),
		icon = ItemType.NETHERRACK.createItemStack(),
		title = Component.text("UHC Level 2"),
		description = Component.text("Construct a nether portal and enter it."),
		criteria = UHCAdvancement.Criteria.impossible()
	)

	val UHC_BLAZE_ROD = UHC_NETHER.append(
		key = UHCPlugin.key("uhc_blaze_rod"),
		icon = ItemType.BLAZE_ROD.createItemStack(),
		title = Component.text("Into Fire"),
		description = Component.text("One to craft a Brewing Stand, one to fuel it."),
		criteria = UHCAdvancement.Criteria.obtainItemList(Material.BLAZE_ROD)
	)

	val UHC_NETHER_WART = UHC_NETHER.append(
		key = UHCPlugin.key("uhc_nether_wart"),
		icon = ItemType.NETHER_WART.createItemStack(),
		title = Component.text("Warts and All"),
		description = Component.text("One to craft a Brewing Stand, one to fuel it."),
		criteria = UHCAdvancement.Criteria.obtainItemList(Material.NETHER_WART)
	)

	val splashHealing2 = ItemType.SPLASH_POTION.createItemStack() { meta ->
		meta.basePotionType = PotionType.STRONG_HEALING
	}

	val UHC_HEALING_2 = UHC_BLAZE_ROD.append(
		key = UHCPlugin.key("uhc_healing_2"),
		icon = splashHealing2,
		title = Component.text("This is Healing, Too"),
		description = Component.text("Brew a splash potion of healing II. The gold standard!"),
		criteria = UHCAdvancement.Criteria.obtainItem(splashHealing2)
	)

	val UHC_FULL_DIAMOND = UHC_DIAMOND.append(
		key = UHCPlugin.key("uhc_full_diamond"),
		icon = ItemType.DIAMOND_CHESTPLATE.createItemStack(),
		title = Component.text("Cover Me in Diamonds"),
		description = Component.text("Every bit counts!"),
		criteria = UHCAdvancement.Criteria.obtainAllItemList(
			Material.DIAMOND_HELMET,
			Material.DIAMOND_CHESTPLATE,
			Material.DIAMOND_LEGGINGS,
			Material.DIAMOND_BOOTS,
		)
	)

	val list = listOf(
		UHC,
		UHC_WOOD,
		UHC_APPLE,
		UHC_FOOD,
		UHC_LEATHER,
		UHC_CANE,
		UHC_SAND,
		UHC_MELON,
		UHC_IRON,
		UHC_GOLD,
		UHC_GLISTERING_MELON,
		UHC_GOLDEN_APPLE,
		UHC_DIAMOND,
		UHC_OBSIDIAN,
		UHC_ENCHANTING_TABLE,
		UHC_SHARP_AXE,
		UHC_NETHER,
		UHC_BLAZE_ROD,
		UHC_NETHER_WART,
		UHC_HEALING_2,
		UHC_FULL_DIAMOND
	)
}
