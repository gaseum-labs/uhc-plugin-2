package org.gaseumlabs.uhcplugin.core

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.gaseumlabs.uhcplugin.world.WorldManager

class TeamChat : Listener {
	@EventHandler
	fun onChatMessage(event: AsyncChatEvent) {
		val player = event.player
		val world = player.world
		val activeGame = UHC.activeGame()
		val game = UHC.game()
		val playerData = activeGame?.playerDatas?.get(player)
		val team = playerData?.team

		if (team != null) {
			val viewers = event.viewers()

			var isGameGlobal: Boolean = false

			var text = (event.message() as? TextComponent)?.content() ?: return

			if (text.startsWith("!")) {
				text = text.substring(1)
				isGameGlobal = true
			}

			event.message(Component.text(text, if (isGameGlobal) NamedTextColor.WHITE else team.color.textColor))

			viewers.clear()
			if (isGameGlobal) {
				viewers.addAll(activeGame.gameWorld.players)
				viewers.addAll(activeGame.netherWorld.players)
			} else {
				viewers.addAll(team.members.mapNotNull { playerData -> playerData.getPlayer() })
			}

			return
		}

		if (world === WorldManager.lobby) {
			val viewers = event.viewers()

			viewers.clear()
			viewers.addAll(WorldManager.lobby.players)

			return
		}

		if (world === game?.gameWorld || world === game?.netherWorld) {
			val viewers = event.viewers()

			viewers.clear()
			viewers.addAll(game.gameWorld.players)
			viewers.addAll(game.netherWorld.players)

			return
		}
	}
}