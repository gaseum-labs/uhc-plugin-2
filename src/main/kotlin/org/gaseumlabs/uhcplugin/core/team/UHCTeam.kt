package org.gaseumlabs.uhcplugin.core.team

import org.bukkit.scoreboard.Team
import java.util.*

class UHCTeam(
	val uuid: UUID,
	val memberUUIDs: ArrayList<UUID>,
	val name: String,
	val color: TeamColor,
	val team: Team,
) {
	val size: Int
		get() = memberUUIDs.size
}
