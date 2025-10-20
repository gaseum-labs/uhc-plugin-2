package org.gaseumlabs.uhcplugin.fix

import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.bukkit.Chunk
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemType

class MelonFix : Listener {
	val chunkQueue = ArrayList<Chunk>()

	@EventHandler
	fun onDropPlayer(event: BlockBreakEvent) {
		if (event.block.type === Material.MELON) {
			if (event.isDropItems && event.player.gameMode !== GameMode.CREATIVE) {
				event.isDropItems = false
				event.block.world.dropItemNaturally(
					event.block.location.add(0.5, 0.5, 0.5),
					ItemType.MELON_SLICE.createItemStack(1)
				)
			}
		}
	}

	@EventHandler
	fun onDropNatural(event: BlockBreakBlockEvent) {
		if (event.block.type === Material.MELON) {
			event.drops.clear()
			event.drops.add(ItemType.MELON_SLICE.createItemStack(1))
		}
	}
}
