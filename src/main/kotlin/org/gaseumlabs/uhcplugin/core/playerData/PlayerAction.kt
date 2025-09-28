package org.gaseumlabs.uhcplugin.core.playerData

import org.bukkit.entity.Zombie

class PlayerActionResult(val online: Boolean, val zombie: Zombie?) {
	inline fun onZombie(execute: (zombie: Zombie) -> Unit) {
		if (zombie != null) execute(zombie)
	}

	inline fun onNoZombie(execute: () -> Unit) {
		if (zombie == null && !online) execute()
	}

	inline fun onOffline(execute: () -> Unit) {
		if (!online) execute()
	}
}
