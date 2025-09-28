package org.gaseumlabs.uhcplugin.core

import net.kyori.adventure.text.Component
import net.minecraft.core.particles.DustParticleOptions
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.Particle
import org.bukkit.damage.DamageSource
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

object Death {
	fun getSlainDeathMessage(killed: OfflinePlayer, killer: Player): Component {
		return Broadcast.game(
			"${killed.name ?: "An unknown player"} was slain by ${killer.name} using "
		).append(getItemName(killer))
	}

	fun offlineDeathMessage(killed: OfflinePlayer): Component {
		return Broadcast.game("${killed.name ?: "An unknown player"} died while offline")
	}

	fun getUnknownDeathMessage(killed: OfflinePlayer): Component {
		return Broadcast.game("${killed.name ?: "An unknown player"} died")
	}

	fun getForfeitDeathMessage(killed: OfflinePlayer): Component {
		return Broadcast.game("${killed.name ?: "An unknown player"} gave up")
	}

	private fun getItemName(killer: Player): Component {
		val item = killer.inventory.itemInMainHand
		if (item.isEmpty) return Broadcast.game("their bare hands")
		return item.effectiveName()
	}

	data class KillerResult(val killerUuid: UUID?, val deathMessage: Component)

	fun getKiller(damageSource: DamageSource, killed: OfflinePlayer, deathMessage: Component?): KillerResult {
		val killerEntity = damageSource.causingEntity
		return if (killerEntity is Player) {
			KillerResult(
				killerEntity.uniqueId,
				getSlainDeathMessage(killed, killerEntity)
			)
		} else {
			KillerResult(
				null,
				deathMessage ?: getUnknownDeathMessage(killed)
			)
		}
	}

	fun dropPlayer(player: Player) {
		val world = player.location.world
		player.inventory.forEach { itemStack: ItemStack? ->
			if (itemStack != null) world.dropItemNaturally(player.location, itemStack)
		}
		if (player.totalExperience > 0) {
			world.spawn(player.location, ExperienceOrb::class.java) { orb ->
				orb.experience = player.totalExperience
			}
		}
		bloodCloud(player.location)
	}

	fun bloodCloud(location: Location) {
		location.world.spawnParticle(
			Particle.DUST,
			location.x,
			location.y + 1.0,
			location.z,
			16,
			0.5,
			1.0,
			0.5,
			0.5,
			DustParticleOptions(0xff0000, 1.0f)
		)
	}
}