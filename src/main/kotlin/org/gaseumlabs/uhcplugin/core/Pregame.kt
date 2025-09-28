package org.gaseumlabs.uhcplugin.core

import org.bukkit.Location
import java.util.*

class Pregame(
	var playerUUIDToLocation: HashMap<UUID, Location>,
	val readyPlayers: HashSet<UUID>,
	val gameConfig: GameConfig,
	var minReadyPlayers: Int,
) {
	companion object {
		fun create(): Pregame {
			return Pregame(
				playerUUIDToLocation = HashMap(),
				readyPlayers = HashSet(),
				gameConfig = GameConfig.createDefault(),
				2
			)
		}
	}

	fun getInitialRadius(): Int {
		return 512;
		//val numPlayers = numReadyPlayers()
		//val area = (169.0 * 256.0) * numPlayers + 2500.0 * numPlayers
		//return floor(sqrt(area) / 2.0).toInt()
	}

	fun getEndgameRadius(): Int {
		return 72;
		//val numPlayers = numReadyPlayers()
		//val area = 2500.0 * numPlayers
		//return floor(sqrt(area) / 2.0).toInt()
	}

	fun makePlayerReady(uuid: UUID): Boolean {
		return readyPlayers.add(uuid)
	}

	fun makePlayerUnready(uuid: UUID): Boolean {
		return readyPlayers.remove(uuid)
	}

	fun numReadyPlayers() = readyPlayers.size
}