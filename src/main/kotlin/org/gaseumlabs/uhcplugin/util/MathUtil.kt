package org.gaseumlabs.uhcplugin.util

object MathUtil {
	fun lerp(start: Double, end: Double, along: Double): Double {
		return (end - start) * along + start
	}

	fun lerp(start: Int, end: Int, along: Double): Int {
		return ((end - start).toDouble() * along + start.toDouble()).toInt()
	}

	fun invLerp(start: Double, end: Double, value: Double): Double {
		return (value - start) / (end - start)
	}

	fun invLerp(start: Int, end: Int, value: Int): Double {
		return (value - start).toDouble() / (end - start).toDouble()
	}
}
