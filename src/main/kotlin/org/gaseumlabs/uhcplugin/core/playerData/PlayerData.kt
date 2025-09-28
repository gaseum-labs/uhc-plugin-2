package org.gaseumlabs.uhcplugin.core.playerData

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import java.util.*

data class OfflineRecord(var zombie: Zombie?, var spectateLocation: Location?, var wipe: Boolean) {
	fun onDeath(location: Location) {
		this.zombie?.remove()
		this.zombie = null
		this.wipe = true
		this.spectateLocation = location
	}

	fun onLogin() {
		this.zombie?.remove()
		this.zombie = null
		this.wipe = false
		this.spectateLocation = null
	}

	fun onLogout(zombie: Zombie) {
		this.zombie?.remove()
		this.zombie = zombie
		this.wipe = false
		this.spectateLocation = null
	}

	fun onSpawn(zombie: Zombie) {
		this.zombie?.remove()
		this.zombie = zombie
		this.wipe = true
		this.spectateLocation = null
	}
}

class PlayerData(
	val uuid: UUID,
	var numDeaths: Int,
	var isActive: Boolean,
) {
	var offlineRecord: OfflineRecord = OfflineRecord(null, null, false)

	fun getPlayer(): Player? {
		return Bukkit.getServer().getPlayer(uuid)
	}

	fun getEntity(): LivingEntity? {
		return getPlayer() ?: offlineRecord.zombie
	}

	fun executeAction(
		action: (Player) -> Unit,
	): PlayerActionResult {
		val player = getPlayer()
		return if (player != null) {
			action(player)
			PlayerActionResult(true, null)
		} else {
			PlayerActionResult(false, offlineRecord.zombie)
		}
	}

	fun getMaxHealth(): Double {
		return (20 - numDeaths * 2).toDouble()
	}

	fun canRespawn(): Boolean {
		return isActive && numDeaths < 10
	}

	companion object {
		fun create(uuid: UUID): PlayerData {
			return PlayerData(
				uuid,
				0,
				true,
			)
		}
	}
}

