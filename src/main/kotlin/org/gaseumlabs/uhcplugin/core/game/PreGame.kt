package org.gaseumlabs.uhcplugin.core.game

import org.bukkit.Location
import org.gaseumlabs.uhcplugin.core.GameConfig
import org.gaseumlabs.uhcplugin.core.team.Teams
import org.gaseumlabs.uhcplugin.core.timer.SingleTimerHolder
import org.gaseumlabs.uhcplugin.core.timer.Timer
import java.util.*

class PreGame(
	var playerUUIDToLocation: HashMap<UUID, Location>,
	val readyPlayers: HashSet<UUID>,
	val gameConfig: GameConfig,
	var minReadyPlayers: Int,
	val teams: Teams,
) : Stage {
	val startGameTimer = SingleTimerHolder<Timer>()

	companion object {
		val INITIAL_RADIUS: Int = 512
		val ENDGAME_RADIUS: Int = 72

		fun createFresh(): PreGame {
			return PreGame(
				playerUUIDToLocation = HashMap(),
				readyPlayers = HashSet(),
				gameConfig = GameConfig.createDefault(),
				2,
				Teams(),
			)
		}
	}

	override fun postTick() {
		startGameTimer.postTick()
	}

	fun makePlayerReady(uuid: UUID): Boolean {
		return readyPlayers.add(uuid)
	}

	fun makePlayerUnready(uuid: UUID): Boolean {
		return readyPlayers.remove(uuid)
	}

	fun numReadyPlayers() = readyPlayers.size
}
