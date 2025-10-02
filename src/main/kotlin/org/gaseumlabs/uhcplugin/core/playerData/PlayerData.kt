package org.gaseumlabs.uhcplugin.core.playerData

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.gaseumlabs.uhcplugin.core.team.UHCTeam
import java.util.*

data class OfflineRecord(var zombie: Zombie?, var spectateLocation: Location?, var wipeMode: WipeMode) {
	fun onDeath(location: Location) {
		this.zombie?.remove()
		this.zombie = null
		this.wipeMode = WipeMode.WIPE
		this.spectateLocation = location
	}

	fun onLogin() {
		this.zombie?.remove()
		this.zombie = null
		this.wipeMode = WipeMode.KEEP
		this.spectateLocation = null
	}

	fun onLogout(zombie: Zombie) {
		this.zombie?.remove()
		this.zombie = zombie
		this.wipeMode = WipeMode.KEEP
		this.spectateLocation = null
	}

	fun onGameSpawn(zombie: Zombie) {
		this.zombie?.remove()
		this.zombie = zombie
		this.wipeMode = WipeMode.HARD_WIPE
		this.spectateLocation = null
	}

	fun onRespawn(zombie: Zombie) {
		this.zombie?.remove()
		this.zombie = zombie
		this.wipeMode = WipeMode.WIPE
		this.spectateLocation = null
	}
}

class PlayerData private constructor(
	val uuid: UUID,
	var numDeaths: Int,
	var isActive: Boolean,
	var team: UHCTeam,
) {
	var offlineRecord: OfflineRecord = OfflineRecord(null, null, WipeMode.KEEP)

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

	fun reset() {
		numDeaths = 0
		isActive = true
		offlineRecord = OfflineRecord(null, null, WipeMode.KEEP)
	}

	companion object {
		fun createInitial(uuid: UUID, team: UHCTeam): PlayerData {
			val playerData = PlayerData(
				uuid,
				0,
				true,
				team,
			)
			team.members.add(playerData)
			return playerData
		}
	}
}

