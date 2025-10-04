package org.gaseumlabs.uhcplugin.core.team

import org.bukkit.entity.LivingEntity
import org.gaseumlabs.uhcplugin.core.game.ActiveGame

class ActiveTeams(teams: List<UHCTeam>) :
	Teams<UHCTeam>(teams, { uuid, memberUUIDs, name, color, team -> UHCTeam(uuid, memberUUIDs, name, color, team) }) {

	data class TeamInNeedResult(val team: UHCTeam, val memberEntity: LivingEntity?)

	fun findTeamInNeedOr(activeGame: ActiveGame, onNewTeam: () -> UHCTeam): TeamInNeedResult {
		val maxTeamSize = list.maxOfOrNull { team -> team.size } ?: return TeamInNeedResult(onNewTeam(), null)
		for (team in list) {
			if (team.size == maxTeamSize) continue
			return TeamInNeedResult(team, getSpawnBuddy(activeGame, team))
		}
		return TeamInNeedResult(onNewTeam(), null)
	}

	fun getSpawnBuddy(activeGame: ActiveGame, team: UHCTeam): LivingEntity? {
		val activeMembers = team.memberUUIDs.map { uuid ->
			activeGame.playerDatas.getActive(uuid)
		}

		return activeMembers.firstOrNull()?.getEntity()
	}
}
