package org.gaseumlabs.uhcplugin.core.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.gaseumlabs.uhcplugin.core.Broadcast
import org.gaseumlabs.uhcplugin.core.CommandUtil
import org.gaseumlabs.uhcplugin.core.UHC

object LobbyCommands {
	fun build(): LiteralCommandNode<CommandSourceStack> {
		val uhcNode = Commands.literal("uhc")

		uhcNode.then(
			Commands.literal("ready")
				.requires { source -> source.sender is Player && UHC.isPregame() }
				.executes(::execReady)
		)

		uhcNode.then(
			Commands.literal("unready")
				.requires { source -> source.sender is Player && UHC.isPregame() }
				.executes(::execUnready)
		)

		uhcNode.then(
			Commands.literal("forceready")
				.requires { source -> source.sender.isOp && UHC.isPregame() }
				.then(
					Commands.argument("player", StringArgumentType.word()).suggests(
						CommandUtil.OfflinePlayerSuggestionProvider(
							Component.text("Force ready player", NamedTextColor.GREEN)
						)
					).executes(::execForceReady)
				)
		)

		uhcNode.then(
			Commands.literal("forceunready")
				.requires { source -> source.sender.isOp && UHC.isPregame() }
				.then(
					Commands.argument("player", StringArgumentType.word()).suggests(
						CommandUtil.OfflinePlayerSuggestionProvider(
							Component.text("Force ready player", NamedTextColor.GREEN)
						)
					).executes(::execForceUnready)
				)
		)

		uhcNode.then(
			Commands.literal("minreadyplayers")
				.requires { source -> source.sender.isOp && UHC.isPregame() }
				.then(
					Commands.literal("set").then(
						Commands.argument("value", IntegerArgumentType.integer(1, 16))
							.executes(::execSetMinReadyPlayers)
					)
				)
		)

		uhcNode.then(
			Commands.literal("readyall")
				.requires { source -> source.sender.isOp && UHC.isPregame() }
				.executes(::execReadyAll)
		)

		uhcNode.then(
			Commands.literal("lobby")
				.requires { source -> source.sender is Player }
				.executes(::execLobby)
		)

		return uhcNode.build()
	}

	private fun broadcastReady(player: OfflinePlayer) {
		Broadcast.broadcast(
			player,
			Broadcast.success("You are now ready for the game"),
			Broadcast.game("${player.name} is now ready")
		)
	}

	private fun broadcastUnready(player: OfflinePlayer) {
		Broadcast.broadcast(
			player,
			Broadcast.success("You are no longer ready for the game"),
			Broadcast.game("${player.name} is no longer ready")
		)
	}

	private fun execReady(context: CommandContext<CommandSourceStack>): Int {
		val player = CommandUtil.getSenderPlayer(context) ?: return CommandUtil.notPlayerError(context)
		val pregame = UHC.getPregame() ?: return CommandUtil.error(context, "Game has already started")

		val isChanged = pregame.makePlayerReady(player.uniqueId)

		if (isChanged) {
			broadcastReady(player)
		} else {
			CommandUtil.error(context, "You are already ready for the game, use /unready to quit")
		}

		return Command.SINGLE_SUCCESS
	}

	private fun execUnready(context: CommandContext<CommandSourceStack>): Int {
		val player = CommandUtil.getSenderPlayer(context) ?: return CommandUtil.notPlayerError(context)
		val pregame = UHC.getPregame() ?: return CommandUtil.error(context, "Game has already started")

		val success = pregame.makePlayerUnready(player.uniqueId)

		if (success) {
			broadcastUnready(player)
		} else {
			CommandUtil.error(context, "You are already not ready")
		}

		return Command.SINGLE_SUCCESS
	}

	private fun execForceReady(context: CommandContext<CommandSourceStack>): Int {
		val forcePlayerName = context.getArgument("player", String::class.java)

		val pregame = UHC.getPregame() ?: return CommandUtil.error(context, "Game has already started")

		val forcePlayer = Bukkit.getOfflinePlayerIfCached(forcePlayerName)
			?: return CommandUtil.error(context, "That player does not exist")

		pregame.makePlayerReady(forcePlayer.uniqueId)

		broadcastReady(forcePlayer)

		return Command.SINGLE_SUCCESS
	}

	private fun execForceUnready(context: CommandContext<CommandSourceStack>): Int {
		val forcePlayerName = context.getArgument("player", String::class.java)

		val pregame = UHC.getPregame() ?: return CommandUtil.error(context, "Game has already started")

		val forcePlayer = Bukkit.getOfflinePlayerIfCached(forcePlayerName)
			?: return CommandUtil.error(context, "That player does not exist")

		pregame.makePlayerUnready(forcePlayer.uniqueId)

		broadcastUnready(forcePlayer)

		return Command.SINGLE_SUCCESS
	}

	private fun execSetMinReadyPlayers(context: CommandContext<CommandSourceStack>): Int {
		val value = context.getArgument("value", Int::class.java)

		val pregame = UHC.getPregame() ?: return CommandUtil.error(context, "Game has already started")

		pregame.minReadyPlayers = value

		CommandUtil.successMessage(context, "Min ready players set to $value")

		return Command.SINGLE_SUCCESS
	}

	private fun execReadyAll(context: CommandContext<CommandSourceStack>): Int {
		val pregame = UHC.getPregame() ?: return CommandUtil.error(context, "Game has already started")

		Bukkit.getOnlinePlayers().forEach { player ->
			if (pregame.makePlayerReady(player.uniqueId)) {
				broadcastReady(player)
			}
		}

		return Command.SINGLE_SUCCESS
	}

	private fun execLobby(context: CommandContext<CommandSourceStack>): Int {
		return Command.SINGLE_SUCCESS
	}
}
