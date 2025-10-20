package org.gaseumlabs.uhcplugin.core

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.RenderType

object HealthObjective {
	const val HEALTH_OBJECTIVE_NAME = "uhc_health"

	fun init() {
		val scoreboard = Bukkit.getScoreboardManager().mainScoreboard

		val objective = scoreboard.getObjective(HEALTH_OBJECTIVE_NAME)
			?: Bukkit.getScoreboardManager().mainScoreboard.registerNewObjective(
				HEALTH_OBJECTIVE_NAME,
				Criteria.HEALTH,
				Component.text("Health"),
				RenderType.HEARTS
			)
		
		objective.displaySlot = DisplaySlot.PLAYER_LIST
	}
}
