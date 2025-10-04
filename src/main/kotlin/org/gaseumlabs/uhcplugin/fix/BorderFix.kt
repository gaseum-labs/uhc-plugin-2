package org.gaseumlabs.uhcplugin.fix

import org.bukkit.entity.Player
import org.gaseumlabs.uhcplugin.core.PlayerManip
import org.gaseumlabs.uhcplugin.core.UHCBorder
import org.gaseumlabs.uhcplugin.core.game.ActiveGame

object BorderFix {
	fun tick(activeGame: ActiveGame) {
		if (activeGame.timer % 20 != 0) return

		activeGame.playerDatas.active.forEach { playerData ->
			val player = playerData.getEntity() ?: return@forEach
			if (player is Player && !PlayerManip.isSquishy(player)) return@forEach
			if (UHCBorder.isOutsideBorder(player.location)) {
				UHCBorder.doDamage(player)
			}
		}
	}
}
