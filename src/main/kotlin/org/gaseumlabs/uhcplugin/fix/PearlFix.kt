package org.gaseumlabs.uhcplugin.fix

import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

class PearlFix : Listener {
	fun onPearl(event: PlayerTeleportEvent) {
		if (event.cause === PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
			event.player.noDamageTicks = 5
		}
	}
}
