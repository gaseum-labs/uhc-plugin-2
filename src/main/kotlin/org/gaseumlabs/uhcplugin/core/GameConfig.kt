package org.gaseumlabs.uhcplugin.core

import kotlinx.serialization.Serializable
import org.gaseumlabs.uhcplugin.core.timer.TickTime
import org.gaseumlabs.uhcplugin.dataStore.DataStore

@Serializable
data class GameConfig(
	var graceDuration: Int,
	var shrinkDuration: Int,
	var collapseTime: Int,
	var finalYLevels: Int,
	var initialRadius: Int,
	var endgameRadius: Int,
	var minReadyPlayers: Int,
) {
	companion object {
		const val filename = "game-config.json"

		fun createDefault(): GameConfig {
			return GameConfig(
				graceDuration = TickTime.ofMinutes(20),
				shrinkDuration = TickTime.ofMinutes(40),
				collapseTime = TickTime.ofMinutes(6),
				finalYLevels = 21,
				initialRadius = 384,
				endgameRadius = 44,
				minReadyPlayers = 4
			)
		}

		fun read(): GameConfig {
			return DataStore.read(filename) {
				createDefault()
			}
		}
	}

	fun write() {
		DataStore.write(filename, this)
	}
}
