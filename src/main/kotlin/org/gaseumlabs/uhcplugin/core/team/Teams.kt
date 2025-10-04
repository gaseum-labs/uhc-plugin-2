package org.gaseumlabs.uhcplugin.core.team

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.scoreboard.Team
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import java.util.*
import kotlin.math.ceil

class Teams {
	private val list = ArrayList<UHCTeam>()

	val teams: List<UHCTeam>
		get() = list

	fun clearTeams() {
		list.forEach { uhcTeam ->
			uhcTeam.team.unregister()
		}
		list.clear()
	}

	fun setRandomTeams(players: List<UUID>, size: Int) {
		clearTeams()

		val shuffledPlayers = players.shuffled()
		val numTeams = ceil(players.size / size.toDouble()).toInt()
		val colors = TeamColor.entries.shuffled()

		list.addAll((0..<numTeams).map { index ->
			val uuid = UUID.randomUUID()
			val color = colors[index]
			val teamName = "Team ${index + 1}"

			val team = Bukkit.getScoreboardManager().mainScoreboard.registerNewTeam(uuid.toString())

			adjustMinecraftTeam(team, teamName, color)

			UHCTeam(uuid, ArrayList(shuffledPlayers.slice(index * size..<(index + 1) * size)), teamName, color, team)
		})
	}

	fun adjustMinecraftTeam(team: Team, name: String, color: TeamColor) {
		team.color(color.teamColor)
		team.displayName(Component.text(name, color.textColor))
		team.prefix(Component.text("█", color.textColor))
		team.suffix(Component.text("█", color.textColor))
	}

	fun addTeam(players: List<UUID>): UHCTeam {
		val color = TeamColor.entries.filter { teamColor ->
			list.none { team -> team.color === teamColor }
		}.random()
		val uuid = UUID.randomUUID()
		val teamName = "Team ${players.size + 1}"

		val team = Bukkit.getScoreboardManager().mainScoreboard.registerNewTeam(uuid.toString())

		val uhcTeam = UHCTeam(uuid, ArrayList(players), teamName, color, team)

		list.add(uhcTeam)

		return uhcTeam
	}

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
