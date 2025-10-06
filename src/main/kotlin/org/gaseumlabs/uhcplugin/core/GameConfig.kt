package org.gaseumlabs.uhcplugin.core

import org.gaseumlabs.uhcplugin.core.timer.TickTime

data class GameConfig(
	var graceDuration: Int,
	var shrinkDuration: Int,
	val collapseTime: Int,
	var finalYLevels: Int,
) {
	companion object {
		fun createDefault(): GameConfig {
			return GameConfig(
				graceDuration = TickTime.ofMinutes(20),
				shrinkDuration = TickTime.ofMinutes(20),
				collapseTime = TickTime.ofMinutes(6),
				finalYLevels = 21
			)
		}
	}
}