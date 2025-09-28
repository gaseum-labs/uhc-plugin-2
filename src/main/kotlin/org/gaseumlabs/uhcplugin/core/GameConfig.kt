package org.gaseumlabs.uhcplugin.core

import org.gaseumlabs.uhcplugin.core.timer.TickTime

data class GameConfig(
	var graceDuration: Int,
	var shrinkDuration: Int,
	var finalYLevels: Int,
) {
	companion object {
		fun createDefault(): GameConfig {
			return GameConfig(
				graceDuration = TickTime.ofMinutes(20),
				shrinkDuration = TickTime.ofMinutes(20),
				finalYLevels = 21
			)
		}
	}
}