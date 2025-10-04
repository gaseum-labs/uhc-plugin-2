package org.gaseumlabs.uhcplugin.core.game

import org.bukkit.World
import org.gaseumlabs.uhcplugin.core.Ledger
import org.gaseumlabs.uhcplugin.core.phase.EndgamePhase
import org.gaseumlabs.uhcplugin.core.phase.Grace
import org.gaseumlabs.uhcplugin.core.phase.Phase
import org.gaseumlabs.uhcplugin.core.phase.Shrink
import org.gaseumlabs.uhcplugin.core.playerData.PlayerDatas
import org.gaseumlabs.uhcplugin.core.team.ActiveTeams
import org.gaseumlabs.uhcplugin.core.timer.MultiTimerHolder
import org.gaseumlabs.uhcplugin.core.timer.RespawnTimer
import java.util.*

class ActiveGame(
	val playerDatas: PlayerDatas,
	val gracePhase: Grace,
	val shrinkPhase: Shrink,
	val endgamePhase: EndgamePhase,
	val initialRadius: Int,
	val finalRadius: Int,
	gameWorld: World,
	netherWorld: World,
	val teams: ActiveTeams,
	val ranked: Boolean,
) : Game(gameWorld, netherWorld) {
	val ledger = Ledger()
	val playerRespawnTimers = MultiTimerHolder<RespawnTimer>()
	val startDate = Date()
	var timer: Int = 0

	override fun destroy() {
		super.destroy()
		teams.clearTeams()
	}

	override fun postTick() {
		++timer
		playerRespawnTimers.postTick()
		endgamePhase.warningTimers.postTick()
	}

	fun getPhase(): Phase {
		return if (timer < gracePhase.duration) {
			gracePhase
		} else if (timer < gracePhase.duration + shrinkPhase.duration) {
			shrinkPhase
		} else {
			endgamePhase
		}
	}

	data class PhaseAlong(val phase: Phase, val timer: Int, val duration: Int) {
		val remaining = duration - timer
		val along = timer.toDouble() / duration.toDouble()
	}

	fun getPhaseAlong(): PhaseAlong {
		return if (timer < gracePhase.duration) {
			PhaseAlong(gracePhase, timer, gracePhase.duration)
		} else if (timer < gracePhase.duration + shrinkPhase.duration) {
			PhaseAlong(shrinkPhase, timer - gracePhase.duration, shrinkPhase.duration)
		} else {
			PhaseAlong(endgamePhase, 0, 1)
		}
	}

	val SHRINK_START_TIME = gracePhase.duration
	val ENDGAME_START_TIME = gracePhase.duration + shrinkPhase.duration

	fun isShrinkStarting(): Boolean {
		return timer == SHRINK_START_TIME
	}

	fun isEndgameStarting(): Boolean {
		return timer == ENDGAME_START_TIME
	}
}
