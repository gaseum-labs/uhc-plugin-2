package org.gaseumlabs.uhcplugin.core.timer

import kotlin.math.ceil

object TickTime {
	fun ofSeconds(seconds: Int): Int = seconds * 20

	fun ofMinutes(minutes: Int): Int = minutes * 60 * 20

	fun toTimeString(ticks: Int): String {
		val numSeconds = ceil(ticks / 20.0).toInt()
		val minutes = numSeconds / 60
		val seconds = numSeconds % 60

		return "${if (minutes == 0) "" else "$minutes minute${if (minutes == 1) "" else "s"} "}$seconds second${if (seconds == 1) "" else "s"}"
	}

	fun regenerationHealth(health: Int): Int = health * 50
	fun regeneration2Health(health: Int): Int = health * 25

	fun poisonHealth(health: Int): Int = health * 25
	fun poison2Health(health: Int): Int = health * 12
}