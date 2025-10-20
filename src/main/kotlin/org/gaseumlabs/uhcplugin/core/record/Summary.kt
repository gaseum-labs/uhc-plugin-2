package org.gaseumlabs.uhcplugin.core.record

import org.bukkit.Bukkit
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import org.gaseumlabs.uhcplugin.core.team.SummaryTeam
import org.gaseumlabs.uhcplugin.util.MathUtil
import java.time.LocalDate
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
	val survivalPlace: Int,
	val killedBy: String?,
)

/*
 * place:
 * on winning team and alive = 1
 * on winning team and dead = 1 + inverse death order of dead winning team players
 * on losing team = inverse death order of
 *
 * within each team, a player's place is the survival order of players in that team
 * ordered by the survival order of the longest surviving player on that team
 */
class Summary private constructor(
	val startDate: LocalDate,
	val ranked: Boolean,
	val players: ArrayList<SummaryPlayer>,
	val ticks: Int,
) {
	companion object {
		fun createUUIDToName(): Map<UUID, String> {
			return Bukkit.getOfflinePlayers()
				.associate { offlinePlayer -> offlinePlayer.uniqueId to (offlinePlayer.name ?: "Unknown Player") }
		}

		fun create(activeGame: ActiveGame, winningTeam: SummaryTeam?, uuidToName: Map<UUID, String>): Summary {
			val teams = activeGame.teams.teams
			val ledger = activeGame.ledger
			val ticks = activeGame.timer
			val startDate = activeGame.startDate
			val ranked = activeGame.ranked

			val players = createPlayers(ledger, teams, ticks, winningTeam, uuidToName)

			return Summary(startDate, ranked, players, ticks)
		}

		fun createPlayers(
			ledger: Ledger,
			teams: List<SummaryTeam>,
			ticks: Int,
			winningTeam: SummaryTeam?,
			uuidToName: Map<UUID, String>,
		): ArrayList<SummaryPlayer> {
			val allPlayers = teams.flatMap { team -> team.members() }
			val numPlayers = allPlayers.size

			val playersBySurvivalOrder = allPlayers.sortedByDescending { playerData ->
				val deathIndex = ledger.kills.indexOfFirst { kill -> kill.player == playerData.uuid() }
				if (deathIndex == -1) 99999999 else deathIndex
			}

			val teamsBySurvivalOrder = teams.sortedBy { team ->
				team.members().minOf { playerData -> playersBySurvivalOrder.indexOf(playerData) }
			}

			val placementOrder = teamsBySurvivalOrder.flatMap { team ->
				team.members().sortedBy { playerData -> playersBySurvivalOrder.indexOf(playerData) }
			}

			val players = placementOrder.mapIndexed { placeIndex, playerData ->
				val team = playerData.team()
				val isWinner = team.uuid() == winningTeam?.uuid()
				val uuid = playerData.uuid()
				val place = if (isWinner) 1 else placeIndex + 1

				val death = ledger.kills.find { kill -> kill.player == uuid }
				val numKills = ledger.kills.count { kill -> kill.killer == uuid }
				val points = (1.0 - MathUtil.invLerp(1, numPlayers, place)) * 10.0 + numKills.toDouble()

				SummaryPlayer(
					uuid = uuid,
					name = uuidToName[uuid] ?: "Unknown Player",
					place = place,
					points = points,
					numKills = numKills,
					aliveTicks = death?.ticks ?: ticks,
					alive = death == null,
					win = isWinner,
					teamName = team.name(),
					survivalPlace = 1 + playersBySurvivalOrder.indexOf(playerData),
					killedBy = death?.killer?.let { killer -> uuidToName[killer] ?: "Unknown Player" }
				)
			} as ArrayList<SummaryPlayer>

			return players
		}
	}
}
