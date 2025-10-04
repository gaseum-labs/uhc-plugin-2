package org.gaseumlabs.uhcplugin.fix

import org.bukkit.Material
import org.bukkit.block.BrewingStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BrewingStartEvent
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import org.gaseumlabs.uhcplugin.core.broadcast.MSG
import org.gaseumlabs.uhcplugin.core.timer.TickTime

class BrewFix : Listener {
	@EventHandler
	fun onBrew(event: BrewEvent) {
		event.results.forEach { itemStack ->
			val potionMeta = itemStack.itemMeta as? PotionMeta ?: return@forEach

			val potionEffect = when (potionMeta.basePotionType) {
				PotionType.POISON ->
					PotionEffect(
						PotionEffectType.WEAKNESS,
						TickTime.poisonHealth(9),
						1
					)

				PotionType.STRONG_POISON ->
					PotionEffect(
						PotionEffectType.WEAKNESS,
						TickTime.poison2Health(14),
						1
					)

				PotionType.LONG_POISON ->
					PotionEffect(
						PotionEffectType.WEAKNESS,
						TickTime.poisonHealth(16),
						1
					)

				PotionType.REGENERATION ->
					PotionEffect(
						PotionEffectType.WEAKNESS,
						TickTime.regenerationHealth(6),
						1
					)

				PotionType.STRONG_REGENERATION ->
					PotionEffect(
						PotionEffectType.WEAKNESS,
						TickTime.regeneration2Health(10),
						1
					)

				PotionType.LONG_REGENERATION ->
					PotionEffect(
						PotionEffectType.WEAKNESS,
						TickTime.regenerationHealth(13),
						1
					)

				PotionType.WEAKNESS ->
					PotionEffect(
						PotionEffectType.WEAKNESS,
						TickTime.ofSeconds(30),
						1
					)

				PotionType.LONG_WEAKNESS ->
					PotionEffect(
						PotionEffectType.WEAKNESS,
						TickTime.ofSeconds(60),
						1
					)

				else -> null
			}

			potionEffect?.let { potionMeta.addCustomEffect(it, false) }

			itemStack.itemMeta = potionMeta
		}
	}

	@EventHandler
	fun onBrewStart(event: BrewingStartEvent) {
		val block = event.block
		if (block.type !== Material.BREWING_STAND) return
		val brewingStand = block.state as? BrewingStand ?: return

		val inventory = brewingStand.inventory

		val ingredient = inventory.ingredient
		if (ingredient?.type !== Material.BLAZE_POWDER) return

		inventory.ingredient = null
		block.location.world.dropItemNaturally(block.location.add(0.5, 1.0, 0.5), ingredient)

		ArrayList(brewingStand.inventory.viewers).forEach { player ->
			player.sendMessage(MSG.error("Strength is banned"))
			player.openInventory.close()
		}
	}
}