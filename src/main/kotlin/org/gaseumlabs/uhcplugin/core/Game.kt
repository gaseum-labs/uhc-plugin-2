package org.gaseumlabs.uhcplugin.core

import org.bukkit.World
import org.gaseumlabs.uhcplugin.core.phase.EndgamePhase
import org.gaseumlabs.uhcplugin.core.phase.Grace
import org.gaseumlabs.uhcplugin.core.phase.Phase
import org.gaseumlabs.uhcplugin.core.phase.Shrink
import org.gaseumlabs.uhcplugin.core.playerData.PlayerDatas
import org.gaseumlabs.uhcplugin.core.timer.MultiTimerHolder
import org.gaseumlabs.uhcplugin.core.timer.RespawnTimer
import java.util.*

class Game(
	val playerDatas: PlayerDatas,
	val gracePhase: Grace,
	val shrinkPhase: Shrink,
	val endgamePhase: EndgamePhase,
	val initialRadius: Int,
	val finalRadius: Int,
	val gameWorld: World,
	val netherWorld: World,
) {
	val ledger = Ledger()
	val playerRespawnTimers = MultiTimerHolder<RespawnTimer>()
	val startDate = Date()
	var timer: Int = 0
	var isDone: Boolean = false

	fun postTick() {
		if (isDone) return
		++timer
		playerRespawnTimers.postTick()
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

	fun isShrinkStarting(): Boolean {
		return timer == gracePhase.duration
	}

	fun isEndgameStarting(): Boolean {
		return timer == gracePhase.duration + shrinkPhase.duration
	}
}