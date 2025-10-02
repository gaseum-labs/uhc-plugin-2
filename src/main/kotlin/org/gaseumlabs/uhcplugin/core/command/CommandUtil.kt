package org.gaseumlabs.uhcplugin.core.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.gaseumlabs.uhcplugin.core.UHC
import org.gaseumlabs.uhcplugin.core.broadcast.Broadcast
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

	fun requiresPregame(source: CommandSourceStack): Boolean {
		return UHC.isPregame()
	}

	fun requiresPregameOp(source: CommandSourceStack): Boolean {
		return source.sender.isOp && UHC.isPregame()
	}

	fun requiresGame(source: CommandSourceStack): Boolean {
		return UHC.isGame()
	}

	fun requiresGameOp(source: CommandSourceStack): Boolean {
		return source.sender.isOp && UHC.isGame()
	}

	fun requiresOp(source: CommandSourceStack): Boolean {
		return source.sender.isOp
	}

	fun createPlayerArgument(name: String, description: String): RequiredArgumentBuilder<CommandSourceStack, String> {
		return Commands.argument(name, StringArgumentType.word()).suggests(
			OfflinePlayerSuggestionProvider(
				Component.text(description, NamedTextColor.GREEN)
			)
		)
	}
}
