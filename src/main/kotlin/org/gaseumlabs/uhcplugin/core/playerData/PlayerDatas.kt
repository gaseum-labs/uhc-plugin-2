package org.gaseumlabs.uhcplugin.core.playerData

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import java.util.*

class PlayerDatas(
	val list: ArrayList<PlayerData>,
	private val uuidToData: HashMap<UUID, PlayerData>,
) {
	fun get(uuid: UUID): PlayerData? {
		return uuidToData[uuid]
	}

	fun get(player: Player): PlayerData? {
		return uuidToData[player.uniqueId]
	}

	fun getActive(uuid: UUID): PlayerData? {
		val playerData = uuidToData[uuid] ?: return null
		if (!playerData.isActive) return null
		return playerData
	}

	fun getActive(player: Player): PlayerData? {
		val playerData = uuidToData[player.uniqueId] ?: return null
		if (!playerData.isActive) return null
		return playerData
	}

	data class ZombieResult(val playerData: PlayerData, val zombie: Zombie)

	fun getByZombie(entity: Entity): ZombieResult? {
		if (entity !is Zombie) return null
		val playerData = list.find { playerData ->
			playerData.isActive && playerData.offlineRecord.zombie?.uniqueId == entity.uniqueId
		} ?: return null
		return ZombieResult(playerData, playerData.offlineRecord.zombie!!)
	}

	val active: List<PlayerData>
		get() = list.filter { playerData -> playerData.isActive }

	companion object {
		fun create(playerDatas: List<PlayerData>): PlayerDatas {
			return PlayerDatas(
				ArrayList(playerDatas),
				playerDatas.associateBy { playerData -> playerData.uuid } as HashMap<UUID, PlayerData>
			)
		}
	}
}