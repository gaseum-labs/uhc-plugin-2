package org.gaseumlabs.uhcplugin.core.playerData

import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.gaseumlabs.uhcplugin.util.coerceEmpty

data class PlayerCapture(
	val itemInMainHand: ItemStack?,
	val itemInOffHand: ItemStack?,
	val helmet: ItemStack?,
	val chestplate: ItemStack?,
	val leggings: ItemStack?,
	val boots: ItemStack?,
	val allItems: List<ItemStack>,
	val location: Location,
	val health: Double,
	val maxHealth: Double,
	val fireTicks: Int,
	val totalExperience: Int,
	val fallDistance: Float,
	val isSmall: Boolean,
) {
	companion object {
		fun create(
			location: Location,
			health: Double,
			maxHealth: Double,
			inventory: PlayerInventory?,
			fireTicks: Int,
			totalExperience: Int,
			fallDistance: Float,
			isSmall: Boolean,
		): PlayerCapture {
			return PlayerCapture(
				inventory?.itemInMainHand?.coerceEmpty(),
				inventory?.itemInOffHand?.coerceEmpty(),
				inventory?.helmet,
				inventory?.chestplate,
				inventory?.leggings,
				inventory?.boots,
				inventory?.toList()?.filter { itemStack -> itemStack != null } ?: emptyList(),
				location,
				health,
				maxHealth,
				fireTicks,
				totalExperience,
				fallDistance,
				isSmall,
			)
		}

		fun createInitial(location: Location, maxHealth: Double): PlayerCapture {
			return PlayerCapture(
				null,
				null,
				null,
				null,
				null,
				null,
				emptyList(),
				location,
				maxHealth,
				maxHealth,
				0,
				0,
				0.0f,
				false
			)
		}
	}
}