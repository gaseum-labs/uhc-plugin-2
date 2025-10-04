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
import org.gaseumlabs.uhcplugin.core.broadcast.MSG
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
			MSG.error("This command can only be used by players")
		)
		return ERROR_CODE
	}

	fun error(context: CommandContext<CommandSourceStack>, message: String): Int {
		context.source.sender.sendMessage(
			MSG.error(message)
		)
		return ERROR_CODE
	}

	fun successMessage(context: CommandContext<CommandSourceStack>, message: String) {
		context.source.sender.sendMessage(
			MSG.success(message)
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

	fun makeRequires(vararg flags: RequiresFlag): (source: CommandSourceStack) -> Boolean {
		return { source ->
			flags.all { flag ->
				when (flag) {
					RequiresFlag.OP -> source.sender.isOp
					RequiresFlag.GAME -> UHC.game() != null
					RequiresFlag.PRE_GAME -> UHC.preGame() != null
					RequiresFlag.ACTIVE_GAME -> UHC.activeGame() != null
					RequiresFlag.POST_GAME -> UHC.postGame() != null
				}
			}
		}
	}

	fun createPlayerArgument(name: String, description: String): RequiredArgumentBuilder<CommandSourceStack, String> {
		return Commands.argument(name, StringArgumentType.word()).suggests(
			OfflinePlayerSuggestionProvider(
				Component.text(description, NamedTextColor.GREEN)
			)
		)
	}

	fun updatePlayers() {
		Bukkit.getOnlinePlayers().forEach { player ->
			player.updateCommands()
		}
	}
}
