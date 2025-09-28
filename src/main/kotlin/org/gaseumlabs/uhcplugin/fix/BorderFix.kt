package org.gaseumlabs.uhcplugin.fix

import org.bukkit.entity.Player
import org.gaseumlabs.uhcplugin.core.Game
import org.gaseumlabs.uhcplugin.core.PlayerManip
import org.gaseumlabs.uhcplugin.core.UHCBorder

object BorderFix {
	fun tick(game: Game) {
		if (game.timer % 20 != 0) return

		game.playerDatas.active.forEach { playerData ->
			val player = playerData.getEntity() ?: return@forEach
			if (player is Player && !PlayerManip.isSquishy(player)) return@forEach
			if (UHCBorder.isOutsideBorder(player.location)) {
				UHCBorder.doDamage(player)
			}
		}
	}
}
