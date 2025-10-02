package org.gaseumlabs.uhcplugin.fix

import org.bukkit.Material
import org.bukkit.block.BrewingStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BrewingStartEvent
import org.bukkit.event.inventory.BrewEvent
import org.gaseumlabs.uhcplugin.core.broadcast.Broadcast

class BrewFix : Listener {
	@EventHandler
	fun onBrew(event: BrewEvent) {
		//TODO poison and regen fix
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
			player.sendMessage(Broadcast.error("Strength is banned"))
			player.openInventory.close()
		}
	}
}