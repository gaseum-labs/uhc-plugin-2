package org.gaseumlabs.uhcplugin.core

import net.minecraft.world.entity.ai.attributes.Attributes
import org.bukkit.*
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.potion.PotionEffect
import org.gaseumlabs.uhcplugin.UHCPlugin

object PlayerManip {
	fun resetPlayer(player: Player, gameMode: GameMode, maxHealth: Double, location: Location) {
		player.gameMode = gameMode
		player.inventory.clear()
		player.clearActiveItem()
		player.setStatistic(Statistic.TIME_SINCE_REST, 0)

		resetAttributes(player, maxHealth)

		player.fallDistance = 0.0f
		player.saturation = 5.0f
		player.foodLevel = 20
		player.health = 20.0
		player.fireTicks = 0
		player.level = 0
		player.exp = 0.0f

		player.enderPearls.forEach { it.remove() }
		player.deathScreenScore = 0
		player.totalExperience = 0
		player.absorptionAmount = 0.0

		player.clearActivePotionEffects()

		player.teleport(location)
	}

	fun resetPlayer(player: Player, offlineZombie: Zombie, maxHealth: Double, wipe: Boolean) {
		player.gameMode = GameMode.SURVIVAL
		if (wipe) player.inventory.clear()
		if (wipe) player.clearActiveItem()
		if (wipe) player.setStatistic(Statistic.TIME_SINCE_REST, 0)

		if (wipe) resetAttributes(player, maxHealth)

		player.fallDistance = offlineZombie.fallDistance
		if (wipe) player.saturation = 5.0f
		if (wipe) player.foodLevel = 20
		player.health = offlineZombie.health
		player.fireTicks = offlineZombie.fireTicks
		if (wipe) player.level = 0
		if (wipe) player.exp = 0.0f

		if (wipe) player.enderPearls.forEach { it.remove() }
		if (wipe) player.deathScreenScore = 0
		if (wipe) player.totalExperience = 0
		player.absorptionAmount = offlineZombie.absorptionAmount

		setPotionEffects(player, offlineZombie.activePotionEffects)
		player.teleport(offlineZombie.location)
	}

	val MODIFIER_KEY = NamespacedKey(UHCPlugin.self, "max_health")

	private fun resetAttributes(player: Player, maxHealth: Double) {
		Registry.ATTRIBUTE.forEach { attribute ->
			val instance = player.getAttribute(attribute) ?: return@forEach

			if (attribute === Attributes.MAX_HEALTH) {
				resetAttribute(instance, maxHealth.toDouble())
			} else {
				resetAttribute(instance, null)
			}
		}
	}

	private fun resetAttribute(instance: AttributeInstance, amount: Double?) {
		for (modifier in instance.modifiers) {
			instance.removeModifier(modifier.key)
		}
		if (amount != null) {
			instance.addModifier(
				AttributeModifier(
					MODIFIER_KEY,
					amount - instance.baseValue,
					AttributeModifier.Operation.ADD_NUMBER
				)
			)
		}
	}

	fun setPotionEffects(player: Player, potionEffects: Collection<PotionEffect>) {
		player.clearActivePotionEffects()
		player.addPotionEffects(potionEffects)
	}

	fun makeSpectator(player: Player, location: Location?) {
		player.gameMode = GameMode.SPECTATOR
		player.health = 20.0
		resetAttributes(player, 20.0)
		location?.let { player.teleport(location) }
	}

	fun isSquishy(player: Player): Boolean {
		return player.gameMode === GameMode.SURVIVAL || player.gameMode === GameMode.ADVENTURE
	}

	fun isCollide(player: Player): Boolean {
		return player.gameMode !== GameMode.SPECTATOR
	}
}
