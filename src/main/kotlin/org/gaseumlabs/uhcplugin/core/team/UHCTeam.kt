package org.gaseumlabs.uhcplugin.core.team

import org.bukkit.scoreboard.Team
import org.gaseumlabs.uhcplugin.core.playerData.PlayerData
import java.util.*

class UHCTeam(
	uuid: UUID,
	memberUUIDs: HashSet<UUID>,
	name: String,
	color: TeamColor,
	team: Team,
) : PreTeam(uuid, memberUUIDs, name, color, team) {
	val members = ArrayList<PlayerData>()

	companion object {
		fun fromPreTeam(preTeam: PreTeam): UHCTeam {
			return UHCTeam(
				preTeam.uuid,
				preTeam.memberUUIDs,
				preTeam.name,
				preTeam.color,
				preTeam.team,
			)
		}
	}
}
