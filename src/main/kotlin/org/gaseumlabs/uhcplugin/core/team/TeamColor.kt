package org.gaseumlabs.uhcplugin.core.team

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.inventory.meta.trim.TrimMaterial

enum class TeamColor(
	val textColor: TextColor,
	val trimMaterial: TrimMaterial,
	val material: Material,
	val dyeColor: DyeColor,
	val teamColor: NamedTextColor,
	val mineralName: String,
) {
	AMETHYST(
		TextColor.color(0x9A5CC6),
		TrimMaterial.AMETHYST,
		Material.AMETHYST_SHARD,
		DyeColor.MAGENTA,
		NamedTextColor.LIGHT_PURPLE,
		"Amethyst"
	),
	COPPER(
		TextColor.color(0xB4684D),
		TrimMaterial.COPPER,
		Material.COPPER_INGOT,
		DyeColor.BROWN,
		NamedTextColor.DARK_RED,
		"Copper",
	),
	DIAMOND(
		TextColor.color(0x6EECD2),
		TrimMaterial.DIAMOND,
		Material.DIAMOND,
		DyeColor.LIGHT_BLUE,
		NamedTextColor.AQUA,
		"Diamond"
	),
	EMERALD(
		TextColor.color(0x11A036),
		TrimMaterial.EMERALD,
		Material.EMERALD,
		DyeColor.LIME,
		NamedTextColor.GREEN,
		"Emerald"
	),
	GOLD(
		TextColor.color(0xDEB12D),
		TrimMaterial.GOLD,
		Material.GOLD_INGOT,
		DyeColor.YELLOW,
		NamedTextColor.YELLOW,
		"Gold"
	),
	IRON(
		TextColor.color(0xECECEC),
		TrimMaterial.IRON,
		Material.IRON_INGOT,
		DyeColor.LIGHT_GRAY,
		NamedTextColor.GRAY,
		"Iron"
	),
	LAPIS(
		TextColor.color(0x416E97),
		TrimMaterial.LAPIS,
		Material.LAPIS_LAZULI,
		DyeColor.BLUE,
		NamedTextColor.BLUE,
		"Lapis"
	),
	QUARTZ(
		TextColor.color(0xE3D4C4),
		TrimMaterial.QUARTZ,
		Material.QUARTZ,
		DyeColor.WHITE,
		NamedTextColor.WHITE,
		"Quartz"
	),
	NETHERITE(
		TextColor.color(0x625859),
		TrimMaterial.NETHERITE,
		Material.NETHERITE_INGOT,
		DyeColor.BLACK,
		NamedTextColor.DARK_GRAY,
		"Netherite"
	),
	REDSTONE(
		TextColor.color(0x971607),
		TrimMaterial.REDSTONE,
		Material.REDSTONE_WIRE,
		DyeColor.RED,
		NamedTextColor.RED,
		"Redstone"
	),
	RESIN(
		TextColor.color(0xFC7812),
		TrimMaterial.RESIN,
		Material.RESIN_BRICK,
		DyeColor.ORANGE,
		NamedTextColor.GOLD,
		"Resin"
	);
}
