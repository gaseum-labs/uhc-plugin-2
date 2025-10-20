package org.gaseumlabs.uhcplugin.util

import kotlin.math.sqrt

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

	fun distance(x0: Double, y0: Double, z0: Double, x1: Double, y1: Double, z1: Double): Double {
		return sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0) + (z1 - z0) * (z1 - z0))
	}

	fun posMod(a: Int, b: Int): Int {
		return ((a % b) + b) % b
	}
}
