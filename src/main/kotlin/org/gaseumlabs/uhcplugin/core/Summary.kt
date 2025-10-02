package org.gaseumlabs.uhcplugin.core

import org.bukkit.Bukkit
import org.gaseumlabs.uhcplugin.core.team.TeamColor
import org.gaseumlabs.uhcplugin.core.team.UHCTeam
import org.gaseumlabs.uhcplugin.util.MathUtil
import java.util.*

data class SummaryPlayer(
	val uuid: UUID,
	val name: String,
	val place: Int,
	val points: Double,
	val numKills: Int,
	val aliveTicks: Int,
	val alive: Boolean,
	val win: Boolean,
	val teamName: String,
	val teamColor: TeamColor,
	val survivalPlace: Int,
)

/*
 * place:
 * on winning team and alive = 1
 * on winning team and dead = 1 + inverse death order of dead winning team players
 * on losing team = inverse death order of
 *
 * within each team, a player's place is the survival order of players in that team
 * ordered by the survival order of the longest surviving player on that team
 *
 */

class Summary(val players: ArrayList<SummaryPlayer>) {
	companion object {
		fun create(ledger: Ledger, teams: List<UHCTeam>, winningTeam: UHCTeam, ticks: Int): Summary {
			val allPlayers = teams.flatMap { team -> team.members }
			val numPlayers = allPlayers.size

			val playersBySurvivalOrder = allPlayers.sortedByDescending { playerData ->
				val deathIndex = ledger.kills.indexOfFirst { kill -> kill.player == playerData }
				if (deathIndex == -1) 99999999 else deathIndex
			}

			val teamsBySurvivalOrder = teams.sortedByDescending { team ->
				team.members.minOf { playerData -> playersBySurvivalOrder.indexOf(playerData) }
			}

			val placementOrder = teamsBySurvivalOrder.flatMap { team ->
				team.members.sortedBy { playerData -> playersBySurvivalOrder.indexOf(playerData) }
			}

			val players = placementOrder.mapIndexed { placeIndex, playerData ->
				val team = playerData.team
				val isWinner = team === winningTeam
				val uuid = playerData.uuid
				val place = if (isWinner) 1 else placeIndex + 1

				val death = ledger.kills.find { kill -> kill.player == uuid }
				val numKills = ledger.kills.count { kill -> kill.killer == uuid }
				val points = (1.0 - MathUtil.invLerp(1, numPlayers, place)) * 10.0 + numKills.toDouble()

				SummaryPlayer(
					uuid = uuid,
					name = Bukkit.getOfflinePlayer(uuid).name ?: "Unknown Player",
					place = place,
					points = points,
					numKills = numKills,
					aliveTicks = death?.ticks ?: ticks,
					alive = death == null,
					win = isWinner,
					teamName = team.name,
					teamColor = team.color,
					survivalPlace = 1 + playersBySurvivalOrder.indexOf(playerData)
				)
			} as ArrayList<SummaryPlayer>

			return Summary(players)
		}
	}
}
