package org.gaseumlabs.uhcplugin.help

import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlayerAdvancement {
	fun wipe(players: Collection<Player>) {
		for (advancement in Bukkit.advancementIterator()) {
			players.forEach { player ->
				val progress = player.getAdvancementProgress(advancement)
				for (criteria in progress.awardedCriteria) {
					progress.revokeCriteria(criteria)
				}
			}
		}
	}

	fun grant(player: Player, uhcAdvancement: UHCAdvancement) {
		val advancement = AdvancementRegistry.getMinecraft(uhcAdvancement)
		val progress = player.getAdvancementProgress(advancement)
		uhcAdvancement.criteriaNames.forEach { criteria ->
			progress.awardCriteria(criteria)
		}
	}
}
