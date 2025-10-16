package org.gaseumlabs.uhcplugin.lootRegen

import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.gaseumlabs.uhcplugin.core.UHC
import org.gaseumlabs.uhcplugin.core.broadcast.Broadcast
import org.gaseumlabs.uhcplugin.core.broadcast.MSG
import org.gaseumlabs.uhcplugin.util.WorldUtil

class LootRegenEvents : Listener {
	@EventHandler
	fun onOpenChest(event: InventoryOpenEvent) {
		if (event.inventory.type !== InventoryType.CHEST) return
		val inventory = event.inventory.holder as? Chest ?: return
		val block = inventory.block

		val game = UHC.activeGame() ?: return
		val lootRegen = game.lootRegen

		val removedChest = lootRegen.chests.removeIf { chest ->
			WorldUtil.blocksEqual(chest.chest, block)
		}

		if (!removedChest) return

		Broadcast.broadcastGame(game, MSG.success("chest destroyed"))

		val player = event.player
		val team = game.playerDatas.get(player)?.team ?: return

		lootRegen.teamLoots.getOrPut(team.uuid) { LootRegen.TeamLoot(0) }.numFound += 1
	}

	//TODO chest destroy events
	//TODO fix loot gen
}
