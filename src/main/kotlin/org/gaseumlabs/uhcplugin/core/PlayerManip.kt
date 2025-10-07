package org.gaseumlabs.uhcplugin.core

import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.potion.PotionEffect
import org.gaseumlabs.uhcplugin.UHCPlugin
import org.gaseumlabs.uhcplugin.core.playerData.WipeMode
import org.gaseumlabs.uhcplugin.help.PlayerAdvancement

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
		player.health = maxHealth
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

	fun resetPlayer(player: Player, offlineZombie: Zombie, maxHealth: Double, wipeMode: WipeMode) {
		val doWipe = wipeMode > WipeMode.KEEP

		player.gameMode = GameMode.SURVIVAL
		if (doWipe) player.inventory.clear()
		if (doWipe) player.clearActiveItem()
		if (doWipe) player.setStatistic(Statistic.TIME_SINCE_REST, 0)

		if (doWipe) resetAttributes(player, maxHealth)

		player.fallDistance = offlineZombie.fallDistance
		if (doWipe) player.saturation = 5.0f
		if (doWipe) player.foodLevel = 20
		player.health = offlineZombie.health.coerceIn(0.0, maxHealth)
		player.fireTicks = offlineZombie.fireTicks
		if (doWipe) player.level = 0
		if (doWipe) player.exp = 0.0f

		if (doWipe) player.enderPearls.forEach { it.remove() }
		if (doWipe) player.deathScreenScore = 0
		if (doWipe) player.totalExperience = 0
		player.absorptionAmount = offlineZombie.absorptionAmount

		setPotionEffects(player, offlineZombie.activePotionEffects)

		if (wipeMode === WipeMode.HARD_WIPE) PlayerAdvancement.wipe(listOf(player))

		player.teleport(offlineZombie.location)
	}

	val MODIFIER_KEY = NamespacedKey(UHCPlugin.self, "max_health")

	private fun resetAttributes(player: Player, maxHealth: Double) {
		Registry.ATTRIBUTE.forEach { attribute ->
			val instance = player.getAttribute(attribute) ?: return@forEach

			if (attribute === Attribute.MAX_HEALTH) {
				resetAttribute(instance, maxHealth)
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
		resetAttributes(player, 20.0)
		player.health = 20.0
		location?.let { player.teleport(location) }
	}

	fun isSquishy(player: Player): Boolean {
		return player.gameMode === GameMode.SURVIVAL || player.gameMode === GameMode.ADVENTURE
	}

	fun isCollide(player: Player): Boolean {
		return player.gameMode !== GameMode.SPECTATOR
	}
}
