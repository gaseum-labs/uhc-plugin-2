package org.gaseumlabs.uhcplugin.core.team

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.scoreboard.Team
import java.util.*
import kotlin.math.ceil

open class Teams<T : PreTeam>(teams: List<T>, val teamConstructor: TeamConstructor<T>) {
	val teams: List<T>
		get() = list

	fun playersTeam(uuid: UUID): T? {
		return teams.find { team -> team.memberUUIDs.contains(uuid) }
	}

	fun playersTeam(player: OfflinePlayer): T? {
		return teams.find { team -> team.memberUUIDs.contains(player.uniqueId) }
	}

	/* ----------------------------------------------------------- */

	fun clearTeams() {
		list.forEach { uhcTeam ->
			uhcTeam.team.unregister()
		}
		list.clear()
	}

	fun fillRandomTeams(players: List<UUID>, size: Int) {
		val nonTeamPlayers = players.filter { uuid -> list.any { team -> team.memberUUIDs.contains(uuid) } }

		val shuffledPlayers = nonTeamPlayers.shuffled()
		val numTeams = ceil(nonTeamPlayers.size / size.toDouble()).toInt()
		val colors = TeamColor.entries.shuffled()

		list.addAll((0..<numTeams).map { index ->
			val uuid = UUID.randomUUID()
			val color = colors[index]
			val teamName = "Team ${index + 1}"

			val team = Bukkit.getScoreboardManager().mainScoreboard.registerNewTeam(uuid.toString())

			adjustMinecraftTeam(team, teamName, color)

			val teamPlayers = HashSet(shuffledPlayers.slice(index * size..<(index + 1) * size))

			teamPlayers.forEach { uuid ->
				team.addPlayer(Bukkit.getOfflinePlayer(uuid))
			}

			teamConstructor(
				uuid,
				teamPlayers,
				teamName,
				color,
				team
			)
		})
	}

	fun createTeam(players: List<UUID>): T {
		removePlayersFromTeam(players)

		val uuid = UUID.randomUUID()
		val teamName = "Team ${players.size + 1}"
		val color = availableColors().firstOrNull() ?: throw Exception("Could not create team")

		val team = Bukkit.getScoreboardManager().mainScoreboard.registerNewTeam(uuid.toString())

		players.forEach { uuid ->
			team.addPlayer(Bukkit.getOfflinePlayer(uuid))
		}

		val uhcTeam = teamConstructor(uuid, HashSet(players), teamName, color, team)

		list.add(uhcTeam)

		cleanupEmptyTeams()

		return uhcTeam
	}

	fun removeFromTeam(uuid: UUID) {
		val oldTeam = playersTeam(uuid) ?: return

		oldTeam.memberUUIDs.remove(uuid)

		cleanupEmptyTeam(oldTeam)
	}

	fun moveToTeam(uuid: UUID, team: T) {
		val oldTeam = playersTeam(uuid)

		team.memberUUIDs.add(uuid)
		team.team.addPlayer(Bukkit.getOfflinePlayer(uuid))

		cleanupEmptyTeam(oldTeam)
	}

	fun destroyTeam(team: T) {
		team.team.unregister()
		list.remove(team)
	}

	/* ----------------------------------------------------------- */

	protected val list = ArrayList<T>(teams)

	private fun adjustMinecraftTeam(team: Team, name: String, color: TeamColor) {
		team.color(color.teamColor)
		team.displayName(Component.text(name, color.textColor))
		team.prefix(Component.text("█", color.textColor))
		team.suffix(Component.text("█", color.textColor))
	}

	private fun availableColors(): List<TeamColor> {
		return TeamColor.entries.filter { teamColor ->
			list.none { team -> team.color === teamColor }
		}.shuffled()
	}

	private fun removePlayersFromTeam(players: List<UUID>) {
		players.forEach { uuid ->
			val playersTeam = playersTeam(uuid) ?: return@forEach

			playersTeam.memberUUIDs.remove(uuid)
			playersTeam.team.removePlayer(Bukkit.getOfflinePlayer(uuid))
		}
	}

	private fun cleanupEmptyTeam(team: T?) {
		if (team == null) return
		if (team.memberUUIDs.isNotEmpty()) return
		team.team.unregister()
		list.remove(team)
	}

	private fun cleanupEmptyTeams() {
		list.removeIf { team ->
			if (team.memberUUIDs.isEmpty()) {
				team.team.unregister()
				list.remove(team)
				true
			} else {
				false
			}
		}
	}
}
