package org.gaseumlabs.uhcplugin.core

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

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

	fun broadcastGame(game: Game, message: Component) {
		game.gameWorld.players.forEach { player ->
			player.sendMessage(message)
		}
		game.netherWorld.players.forEach { player ->
			player.sendMessage(message)
		}
	}

	fun game(text: String) = Component.text(text, TextColor.color(0xfc9d03))

	fun success(text: String): Component = Component.text(text, TextColor.color(0x1cc916))

	fun error(text: String): Component = Component.text(text, TextColor.color(0xe61014))
}