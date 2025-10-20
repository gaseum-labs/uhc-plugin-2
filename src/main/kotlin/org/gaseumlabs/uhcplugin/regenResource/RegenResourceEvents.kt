package org.gaseumlabs.uhcplugin.regenResource

import com.destroystokyo.paper.event.block.BlockDestroyEvent
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.world.LootGenerateEvent
import org.bukkit.inventory.ItemType
import org.bukkit.loot.LootTables
import org.gaseumlabs.uhcplugin.core.UHC
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import org.gaseumlabs.uhcplugin.core.playerData.PlayerData
import org.gaseumlabs.uhcplugin.util.MathUtil
import org.gaseumlabs.uhcplugin.util.WorldUtil
import kotlin.random.Random

class RegenResourceEvents : Listener {
	@EventHandler
	fun onGenerateLoot(event: LootGenerateEvent) {
		if (event.lootTable.key == LootTables.NETHER_BRIDGE.key) {

			event.loot.clear()

			event.loot.add(ItemType.NETHER_WART.createItemStack())
			if (Random.nextDouble() < 0.50) event.loot.add(ItemType.GOLD_INGOT.createItemStack())
			if (Random.nextDouble() < 0.33) event.loot.add(ItemType.SADDLE.createItemStack())
			if (Random.nextDouble() < 0.25) event.loot.add(ItemType.GOLDEN_HORSE_ARMOR.createItemStack())
			if (Random.nextDouble() < 0.20) event.loot.add(ItemType.IRON_INGOT.createItemStack())
			if (Random.nextDouble() < 0.20) event.loot.add(ItemType.DIAMOND.createItemStack())
			if (Random.nextDouble() < 0.20) event.loot.add(ItemType.FLINT_AND_STEEL.createItemStack())
			if (Random.nextDouble() < 0.20) event.loot.add(ItemType.IRON_HORSE_ARMOR.createItemStack())
			if (Random.nextDouble() < 0.20) event.loot.add(ItemType.GOLDEN_SWORD.createItemStack())
			if (Random.nextDouble() < 0.20) event.loot.add(ItemType.GOLDEN_CHESTPLATE.createItemStack())
			if (Random.nextDouble() < 0.20) event.loot.add(ItemType.DIAMOND_HORSE_ARMOR.createItemStack())
			if (Random.nextDouble() < 0.15) event.loot.add(ItemType.OBSIDIAN.createItemStack(4))
			if (Random.nextDouble() < 0.07) event.loot.add(ItemType.RIB_ARMOR_TRIM_SMITHING_TEMPLATE.createItemStack())
		}
	}

	@EventHandler
	fun onOpenChest(event: InventoryOpenEvent) {
		val activeGame = UHC.activeGame() ?: return
		if (event.inventory.type !== InventoryType.CHEST) return
		val inventory = event.inventory.holder as? Chest ?: return
		val block = inventory.block

		collectFeature(activeGame, block, activeGame.playerDatas.get(event.player))
	}

	fun onDestroyChest(event: BlockDestroyEvent) {
		val activeGame = UHC.activeGame() ?: return
		val block = event.block

		collectFeature(activeGame, block, findNearPlayer(activeGame, block))
	}

	data class NearPlayer(val playerData: PlayerData, val distance: Double)

	fun findNearPlayer(activeGame: ActiveGame, block: Block): PlayerData? {
		val (nearPlayerData, nearDistance) = activeGame.playerDatas.active.mapNotNull { playerData ->
			val player = playerData.getPlayer() ?: return@mapNotNull null
			if (player.world !== block.world) return@mapNotNull null
			val location = player.location
			NearPlayer(
				playerData,
				MathUtil.distance(block.x + 0.5, block.y + 0.5, block.z + 0.5, location.x, location.y, location.z)
			)
		}.minByOrNull { (_, distance) ->
			distance
		} ?: return null

		if (nearDistance > 16.0) return null

		return nearPlayerData
	}

	fun collectFeature(activeGame: ActiveGame, block: Block, playerData: PlayerData?) {
		val lootRegen = activeGame.lootRegen

		for (resourceData in lootRegen.resourceDatas) {
			val removedChest = resourceData.features.removeIf { chest ->
				WorldUtil.blocksEqual(chest.block, block)
			}

			if (!removedChest) continue

			//Broadcast.broadcast { MSG.success("chest destroyed") }

			val team = playerData?.team ?: return

			resourceData.teamLoots.getOrPut(team.uuid) { RegenResourceManager.TeamLoot(0) }.numFound += 1

			//Broadcast.broadcast { MSG.success("${playerData.team.name} found a chest (open)") }
		}
	}
}
