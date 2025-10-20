package org.gaseumlabs.uhcplugin.regenResource

class ChunkGrid(val xRange: IntRange, val zRange: IntRange) {
	val grid0 = IntArray(xRange.count() * zRange.count())
	val grid1 = IntArray(xRange.count() * zRange.count())
	var currentGrid: IntArray = grid0
	var oldGrid: IntArray = grid1
	val width = xRange.count()

	fun isInside(x: Int, z: Int): Boolean {
		return x in xRange && z in zRange
	}

	fun get(x: Int, z: Int): Int {
		if (!isInside(x, z)) return 0

		val gx = x - xRange.first
		val gz = z - zRange.first

		return currentGrid[gx * width + gz]
	}

	fun set(x: Int, z: Int, value: Int) {
		if (!isInside(x, z)) return

		val gx = x - xRange.first
		val gz = z - zRange.first

		currentGrid[gx * width + gz] = value
	}

	fun swap() {
		if (currentGrid === grid0) {
			currentGrid = grid1
			oldGrid = grid0
		} else {
			currentGrid = grid0
			oldGrid = grid1
		}
		for (i in currentGrid.indices) {
			currentGrid[i] = EMPTY
		}
	}

	inline fun forEach(action: (x: Int, z: Int, oldValue: Int, newValue: Int) -> Unit) {
		for (i in currentGrid.indices) {
			val x = i / width + xRange.first
			val z = i % width + zRange.first
			action(x, z, oldGrid[i], currentGrid[i])
		}
	}

	companion object {
		const val EMPTY = 0
		const val IN_PLAY = 1
	}
}
