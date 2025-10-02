package org.gaseumlabs.uhcplugin.core.broadcast

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.gaseumlabs.uhcplugin.core.Game

object Broadcast {
	fun broadcast(playerToMessage: (Player) -> Component?) {
		Bukkit.getOnlinePlayers().forEach { player ->
			playerToMessage(player)?.let { player.sendMessage(it) }
		}
	}

	fun broadcast(player: OfflinePlayer, playerMessage: Component, restMessage: Component) {
		Bukkit.getOnlinePlayers().forEach { audience ->
			if (audience === player) audience.sendMessage(playerMessage)
			else audience.sendMessage(restMessage)
		}
	}

	fun broadcast(message: Component) {
		Bukkit.getOnlinePlayers().forEach { audience ->
			audience.sendMessage(message)
		}
	}

	fun broadcastGame(game: Game, vararg messages: Component) {
		game.gameWorld.players.forEach { player ->
			messages.forEach { player.sendMessage(it) }
		}
		game.netherWorld.players.forEach { player ->
			messages.forEach { player.sendMessage(it) }
		}
	}
}