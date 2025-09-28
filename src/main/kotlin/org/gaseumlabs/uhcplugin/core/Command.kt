package org.gaseumlabs.uhcplugin.core

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

object CommandUtil {
	const val ERROR_CODE = 0

	fun getSenderPlayer(context: CommandContext<CommandSourceStack>): Player? {
		val sender = context.source.sender
		if (sender is Player) return sender
		return null
	}

	fun notPlayerError(context: CommandContext<CommandSourceStack>): Int {
		context.source.sender.sendMessage(
			Broadcast.error("This command can only be used by players")
		)
		return ERROR_CODE
	}

	fun error(context: CommandContext<CommandSourceStack>, message: String): Int {
		context.source.sender.sendMessage(
			Broadcast.error(message)
		)
		return ERROR_CODE
	}

	fun successMessage(context: CommandContext<CommandSourceStack>, message: String) {
		context.source.sender.sendMessage(
			Broadcast.success(message)
		)
	}

	class OfflinePlayerSuggestionProvider(val tooltip: Component) : SuggestionProvider<CommandSourceStack> {
		override fun getSuggestions(
			context: CommandContext<CommandSourceStack>,
			builder: SuggestionsBuilder,
		): CompletableFuture<Suggestions> {
			for (player in Bukkit.getOfflinePlayers()) {
				if (player.name != null) builder.suggest(
					player.name,
					MessageComponentSerializer.message().serialize(tooltip)
				)
			}
			return builder.buildFuture()
		}
	}
}
