package org.gaseumlabs.uhcplugin.core.team

import org.bukkit.scoreboard.Team
import org.gaseumlabs.uhcplugin.core.playerData.PlayerData
import org.gaseumlabs.uhcplugin.core.playerData.SummaryPlayerData
import java.util.*

class UHCTeam(
	uuid: UUID,
	memberUUIDs: HashSet<UUID>,
	name: String,
	color: TeamColor,
	team: Team,
) : PreTeam(uuid, memberUUIDs, name, color, team), SummaryTeam {
	val members = ArrayList<PlayerData>()

	override fun uuid(): UUID = uuid
	override fun name(): String = name
	override fun members(): List<SummaryPlayerData> = members

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
