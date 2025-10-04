package org.gaseumlabs.uhcplugin.core.playerData

import io.papermc.paper.event.entity.EntityMoveEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.loot.LootContext
import org.bukkit.loot.LootTable
import org.bukkit.loot.LootTables
import org.bukkit.persistence.PersistentDataType
import org.gaseumlabs.uhcplugin.UHCPlugin
import org.gaseumlabs.uhcplugin.core.Death
import org.gaseumlabs.uhcplugin.core.UHC
import java.util.*

object OfflineZombie {
	val KEY_UUID = NamespacedKey(UHCPlugin.self, "offline_zombie_uuid")
	val KEY_EXPERIENCE = NamespacedKey(UHCPlugin.self, "offline_zombie_exp")

	class ZombieLootTable(val items: List<ItemStack>) : LootTable {
		override fun fillInventory(inventory: Inventory, random: Random?, context: LootContext) {}
		override fun getKey() = LootTables.ZOMBIE.key
		override fun populateLoot(random: Random?, context: LootContext) = items
	}

	fun spawn(uuid: UUID, capture: PlayerCapture): Zombie {
		val zombie = capture.location.world.spawn(capture.location, Zombie::class.java) { zombie ->
			if (capture.isSmall) zombie.setBaby() else zombie.setAdult()
			zombie.setAI(false)
			zombie.persistentDataContainer.set(KEY_UUID, PersistentDataType.STRING, uuid.toString())
			zombie.persistentDataContainer.set(KEY_EXPERIENCE, PersistentDataType.INTEGER, capture.totalExperience)

			zombie.lootTable = ZombieLootTable(capture.allItems)

			zombie.equipment.setItemInMainHand(capture.itemInMainHand)
			zombie.equipment.setItemInOffHand(capture.itemInOffHand)
			zombie.equipment.setChestplate(capture.chestplate, true)
			zombie.equipment.setLeggings(capture.leggings, true)
			zombie.equipment.setBoots(capture.boots, true)

			zombie.canPickupItems = false

			zombie.equipment.bootsDropChance = 0.0f
			zombie.equipment.helmetDropChance = 0.0f
			zombie.equipment.leggingsDropChance = 0.0f
			zombie.equipment.chestplateDropChance = 0.0f
			zombie.equipment.itemInMainHandDropChance = 0.0f
			zombie.equipment.itemInOffHandDropChance = 0.0f

			zombie.fireTicks = capture.fireTicks
			zombie.fallDistance = capture.fallDistance

			val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
			offlinePlayer.name?.let { name ->
				zombie.customName(Component.text(name))
				zombie.isCustomNameVisible = true
			}
			val playerHead = ItemStack(Material.PLAYER_HEAD)
			val meta = playerHead.itemMeta as SkullMeta
			meta.owningPlayer = offlinePlayer
			playerHead.itemMeta = meta
			zombie.equipment.setHelmet(playerHead, true)

			zombie.health = capture.health
			zombie.getAttribute(Attribute.MAX_HEALTH)?.baseValue = capture.maxHealth
		}

		zombie.chunk.isForceLoaded = true

		return zombie
	}

	class Events : Listener {
		@EventHandler
		fun onKillEntity(event: EntityDeathEvent) {
			val activeGame = UHC.activeGame() ?: return
			val (playerData, zombie) = activeGame.playerDatas.getByZombie(event.entity) ?: return

			val (killerUuid, deathMessage) = Death.getKiller(
				event.damageSource,
				Bukkit.getOfflinePlayer(playerData.uuid),
				Death.offlineDeathMessage(Bukkit.getOfflinePlayer(playerData.uuid))
			)

			UHC.onPlayerDeath(activeGame, playerData, zombie.location, killerUuid, deathMessage, false)

			event.droppedExp = zombie.persistentDataContainer.get(KEY_EXPERIENCE, PersistentDataType.INTEGER) ?: 0
		}

		@EventHandler
		fun onMoveEntity(event: EntityMoveEvent) {
			val game = UHC.activeGame() ?: return
			val (_, zombie) = game.playerDatas.getByZombie(event.entity) ?: return

			val newChunk = zombie.chunk
			newChunk.isForceLoaded = true
		}

		@EventHandler
		fun onCombust(event: EntityCombustEvent) {
			val game = UHC.activeGame() ?: return
			val (_, zombie) = game.playerDatas.getByZombie(event.entity) ?: return

			event.isCancelled = true;
		}
	}
}