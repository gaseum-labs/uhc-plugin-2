package org.gaseumlabs.uhcplugin.fix

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BrewingStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BrewingStartEvent
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.inventory.BrewerInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import org.gaseumlabs.uhcplugin.UHCPlugin
import org.gaseumlabs.uhcplugin.core.broadcast.MSG
import org.gaseumlabs.uhcplugin.core.timer.TickTime

class BrewFix : Listener {
	companion object {
		fun createCustomPotion(
			itemType: ItemType.Typed<PotionMeta>,
			potionId: CustomPotionId,
		): ItemStack {
			return itemType.createItemStack { meta ->
				meta.customName(
					Component.text(
						"${if (itemType === ItemType.SPLASH_POTION) "Splash " else ""}Potion of ${potionId.name}",
						Style.style().decoration(TextDecoration.ITALIC, false).build()
					)
				)
				meta.color = potionId.potionEffectType.color
				meta.addCustomEffect(
					PotionEffect(potionId.potionEffectType, potionId.duration, potionId.amplifier),
					false
				)
			}
		}

		sealed class PotionId;

		class CustomPotionId(
			val potionEffectType: PotionEffectType,
			val name: String,
			val duration: Int,
			val amplifier: Int,
		) : PotionId()

		class TypePotionId(
			val potionType: PotionType,
		) : PotionId()

		class PotionRecipe(
			val ingredient: Material,
			val potion: PotionId,
			val result: CustomPotionId,
			val isReal: Boolean = false,
		)

		val ID_WATER = TypePotionId(PotionType.WATER)
		val ID_AWKWARD = TypePotionId(PotionType.AWKWARD)

		val ID_POISON = CustomPotionId(PotionEffectType.POISON, "Poison", TickTime.poisonHealth(9), 0)
		val ID_POISON_LONG = CustomPotionId(PotionEffectType.POISON, "Poison", TickTime.poisonHealth(16), 0)
		val ID_POISON_STRONG = CustomPotionId(PotionEffectType.POISON, "Poison", TickTime.poison2Health(14), 1)

		val ID_REGENERATION =
			CustomPotionId(PotionEffectType.REGENERATION, "Regeneration", TickTime.regenerationHealth(6), 0)
		val ID_REGENERATION_LONG =
			CustomPotionId(PotionEffectType.REGENERATION, "Regeneration", TickTime.regenerationHealth(10), 0)
		val ID_REGENERATION_STRONG =
			CustomPotionId(PotionEffectType.REGENERATION, "Regeneration", TickTime.regeneration2Health(13), 1)

		val ID_WEAKNESS = CustomPotionId(PotionEffectType.WEAKNESS, "Weakness", TickTime.ofSeconds(30), 0)
		val ID_WEAKNESS_LONG = CustomPotionId(PotionEffectType.WEAKNESS, "Weakness", TickTime.ofSeconds(60), 0)

		val RECIPE_POISON = PotionRecipe(
			Material.SPIDER_EYE,
			ID_AWKWARD,
			ID_POISON,
			isReal = true
		)
		val RECIPE_POISON_LONG = PotionRecipe(
			Material.REDSTONE,
			ID_POISON,
			ID_POISON_LONG
		)
		val RECIPE_POISON_STRONG = PotionRecipe(
			Material.GLOWSTONE_DUST,
			ID_POISON,
			ID_POISON_STRONG
		)

		val RECIPE_REGENERATION = PotionRecipe(
			Material.GHAST_TEAR,
			ID_AWKWARD,
			ID_REGENERATION,
			isReal = true
		)
		val RECIPE_REGENERATION_LONG = PotionRecipe(
			Material.REDSTONE,
			ID_REGENERATION,
			ID_REGENERATION_LONG
		)
		val RECIPE_REGENERATION_STRONG = PotionRecipe(
			Material.GLOWSTONE_DUST,
			ID_REGENERATION,
			ID_REGENERATION_STRONG
		)

		val RECIPE_WEAKNESS = PotionRecipe(
			Material.FERMENTED_SPIDER_EYE,
			ID_WATER,
			ID_WEAKNESS,
			isReal = true
		)
		val RECIPE_WEAKNESS_LONG = PotionRecipe(
			Material.REDSTONE,
			ID_WEAKNESS,
			ID_WEAKNESS_LONG
		)

		val recipes = arrayOf(
			RECIPE_POISON,
			RECIPE_POISON_LONG,
			RECIPE_POISON_STRONG,
			RECIPE_REGENERATION,
			RECIPE_REGENERATION_LONG,
			RECIPE_REGENERATION_STRONG,
			RECIPE_WEAKNESS,
			RECIPE_WEAKNESS_LONG
		)
	}

	val brewTasks = HashMap<BrewingStand, Int>()

	fun setBrewTask(brewingStand: BrewingStand) {
		var timer = 400

		cancelBrewTask(brewingStand)

		brewTasks[brewingStand] = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.self, {
			if (brewingStand.block.type !== Material.BREWING_STAND) {
				cancelBrewTask(brewingStand)
				return@scheduleSyncRepeatingTask
			}

			--timer
			brewingStand.brewingTime = timer

			if (timer <= 0) {
				replaceItems(brewingStand.inventory)

				--brewingStand.fuelLevel

				cancelBrewTask(brewingStand)
			}
		}, 0, 1)
	}

	fun isBrewing(brewingStand: BrewingStand): Boolean {
		return brewTasks[brewingStand] != null
	}

	fun cancelBrewTask(brewingStand: BrewingStand) {
		val taskId = brewTasks.remove(brewingStand)
		if (taskId != null) Bukkit.getScheduler().cancelTask(taskId)
	}

	private fun internalOnInventory(inventory: Inventory) {
		if (inventory !is BrewerInventory) return
		val brewingStand = inventory.holder?.block?.getState(false) as? BrewingStand? ?: return

		/* before the event resolves, was there already an ingredient */
		/* used to tell if the ingredient is just now being added or removed */
		val oldIngredient = inventory.ingredient

		Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.self) {
			val newIngredient = inventory.ingredient
			val matchResult = matchRecipe(inventory)

			if (matchResult?.recipe?.isReal == true) return@scheduleSyncDelayedTask

			if (brewingStand.fuelLevel <= 0 || matchResult == null) {
				cancelBrewTask(brewingStand)
			} else if (oldIngredient?.type != newIngredient?.type) {
				/* restart brew with new ingredient */
				setBrewTask(brewingStand)
			}
		}
	}

	data class MatchResult(val recipe: PotionRecipe, val matchingSlots: List<Int>)

	fun matchRecipe(inventory: BrewerInventory): MatchResult? {
		for (recipe in recipes) {
			if (inventory.ingredient?.type !== recipe.ingredient) continue

			val slots = (0..2).toList().filter { slot ->
				val itemStack = inventory.getItem(slot)
				val potionMeta = itemStack?.itemMeta as? PotionMeta ?: return@filter false

				when (val potionId = recipe.potion) {
					is TypePotionId -> {
						potionId.potionType === potionMeta.basePotionType
					}

					is CustomPotionId -> {
						val customEffect = potionMeta.customEffects.firstOrNull()
						customEffect != null && customEffect.type === potionId.potionEffectType && customEffect.duration == potionId.duration && customEffect.amplifier == potionId.amplifier
					}
				}
			}

			if (slots.isNotEmpty()) return MatchResult(recipe, slots)
		}

		return null
	}

	/*
	 * events fired whenever the player clicks in a brewing inventory
	 * or whenever a hopper dispenses into a brewing inventory
	 */

	@EventHandler
	fun onInventoryDrag(event: InventoryDragEvent) {
		internalOnInventory(event.inventory)
	}

	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		internalOnInventory(event.inventory)
	}

	@EventHandler
	fun onHopper(event: InventoryMoveItemEvent) {
		internalOnInventory(event.destination)
	}

	fun replaceItems(inventory: BrewerInventory): Boolean {
		val matchResult = matchRecipe(inventory) ?: return false

		matchResult.matchingSlots.forEach { slot ->
			val material = inventory.getItem(slot)?.type
			val itemType = if (material === Material.SPLASH_POTION) ItemType.SPLASH_POTION else ItemType.POTION
			inventory.setItem(slot, createCustomPotion(itemType, matchResult.recipe.result))
		}

		/* use up the ingredient */
		inventory.ingredient?.let { --it.amount }

		return true
	}

	fun matchPotionId(itemStack: ItemStack): CustomPotionId? {
		val potionMeta = itemStack.itemMeta as? PotionMeta ?: return null
		val customEffect = potionMeta.customEffects.firstOrNull() ?: return null

		for (recipe in recipes) {
			val potionId = recipe.result

			if (customEffect.type === potionId.potionEffectType && customEffect.duration == potionId.duration && customEffect.amplifier == potionId.amplifier) {
				return potionId
			}
		}

		return null
	}

	/**
	 * this is called whenever a non-fake brew is completed
	 * EX: when the default awkward to poison is brewed
	 *
	 * replaces the results of the brew
	 */
	@EventHandler
	fun onBrew(event: BrewEvent) {
		if (event.contents.ingredient?.type === Material.GUNPOWDER) {
			for (slot in 0..2) {
				val itemStack = event.contents.getItem(slot) ?: continue
				val potionId = matchPotionId(itemStack) ?: continue
				event.results[slot] = createCustomPotion(ItemType.SPLASH_POTION, potionId)
			}
		} else {
			if (replaceItems(event.contents)) {
				event.isCancelled = true
				event.block.world.playSound(
					event.block.location.toCenterLocation(),
					Sound.BLOCK_BREWING_STAND_BREW,
					1.0f,
					1.0f
				)
			}
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
