package org.gaseumlabs.uhcplugin.core.timer

data class PartialTickResult(val ticks: Int, val duration: Int)

data class TickResult<T : Timer>(val timer: T, val ticks: Int, val duration: Int) {
	fun isDone() = ticks >= duration
	fun remaining() = duration - ticks
	fun along() = ticks.toDouble() / duration.toDouble()

	companion object {
		fun <T : Timer> fromPartial(timer: T, partial: PartialTickResult): TickResult<T> =
			TickResult(timer, partial.ticks, partial.duration)
	}
}

interface Timer {
	fun get(): PartialTickResult
	fun postTick()
	fun isDone(): Boolean
}

open class CountdownTimer(val duration: Int) : Timer {
	var ticks: Int = 0

	override fun get(): PartialTickResult {
		return PartialTickResult(ticks, duration)
	}

	override fun postTick() {
		++ticks
	}

	override fun isDone() = ticks >= duration
}

open class CountUpTimer() : Timer {
	var ticks: Int = 0

	override fun get(): PartialTickResult {
		return PartialTickResult(ticks, 0)
	}

	override fun postTick() {
		++ticks
	}

	override fun isDone() = false
}

class SingleTimerHolder<T : Timer>() {
	private var timer: T? = null
	private var bufferedTimer: T? = null

	fun isActive() = timer != null
	fun get(): TickResult<T>? = timer?.let { TickResult.fromPartial(it, it.get()) }

	fun postTick() {
		timer?.let { timer ->
			if (timer.isDone()) {
				this.timer = null
			}
		}
		if (bufferedTimer != null) this.timer = bufferedTimer
		this.timer?.postTick()
	}

	fun set(timer: T) {
		this.bufferedTimer = timer;
	}

	fun cancel() {
		this.timer = null
	}
}

class MultiTimerHolder<T : Timer>() {
	private var timers = ArrayList<T>()
	private var addedTimers = ArrayList<T>()

	fun get(): List<TickResult<T>> {
		return timers.map { timer -> TickResult.fromPartial(timer, timer.get()) }
	}

	fun postTick() {
		timers.addAll(addedTimers)
		addedTimers.clear()

		timers.removeIf { timer -> timer.isDone() }

		timers.forEach { timer -> timer.postTick() }
	}

	fun add(timer: T) {
		addedTimers.add(timer)
	}

	fun remove(timer: T): Boolean {
		return addedTimers.remove(timer) || timers.remove(timer)
	}

	fun reset() {
		addedTimers.clear()
		timers.clear()
	}
}


