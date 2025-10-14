package org.gaseumlabs.uhcplugin.help

import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.gaseumlabs.uhcplugin.core.UHC
import org.gaseumlabs.uhcplugin.core.broadcast.MSG
import org.gaseumlabs.uhcplugin.core.game.ActiveGame

class ChatHelp : Listener {
	@EventHandler
	fun onBreakBlock(event: BlockBreakEvent) {
		val player = event.player
		val activeGame = UHC.activeGame() ?: return
		val playerData = activeGame.playerDatas.getActive(player) ?: return

		playerData.chatHelp.forEach { chatHelp ->
			if (chatHelp.found) return@forEach
			chatHelp.requirements.forEach { requirement ->
				if (requirement.found) return@forEach
				if (requirement.requirement is BreakBlockRequirement) {
					if (event.block.type === requirement.requirement.material) {
						aquireRequirement(player, chatHelp, requirement)
					}
				}
			}
		}
	}

	companion object {
		fun createInstances(): List<ChatHelpInstance> {
			return ChatHelpItem.entries.map { item ->
				ChatHelpInstance(
					item,
					item.requirements.map { requirement ->
						RequirementInstace(
							requirement,
							false
						)
					} as ArrayList<RequirementInstace>,
					false)
			}
		}

		fun tick(activeGame: ActiveGame) {
			val timer = activeGame.timer

			activeGame.playerDatas.active.forEach { playerData ->
				val player = playerData.getPlayer() ?: return@forEach
				val chatHelps = playerData.chatHelp
				val checkIndex = timer % chatHelps.size

				val chatHelp = chatHelps[checkIndex]
				if (chatHelp.found) return@forEach

				chatHelp.requirements.forEach { requirement ->
					if (requirement.found) return@forEach
					if (requirement.requirement !is TickChatHelpRequirement) return@forEach
					if (!requirement.requirement.check(player)) return@forEach

					aquireRequirement(player, chatHelp, requirement)
				}
			}
		}

		fun aquireRequirement(player: Player, chatHelp: ChatHelpInstance, requirement: RequirementInstace) {
			requirement.found = true
			if (chatHelp.requirements.all { instance -> instance.found }) {
				chatHelp.found = true
				player.sendMessage(MSG.help("UHC HELP: " + chatHelp.item.message))
				player.playSound(
					player.location,
					Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
					1.0f,
					1.0f
				)
			}
		}
	}
}
