package org.gaseumlabs.uhcplugin.core.game

sealed interface Stage {
	abstract fun postTick()
}
