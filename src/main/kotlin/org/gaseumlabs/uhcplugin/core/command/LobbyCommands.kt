package org.gaseumlabs.uhcplugin.core.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.gaseumlabs.uhcplugin.core.PlayerManip
import org.gaseumlabs.uhcplugin.core.UHC
import org.gaseumlabs.uhcplugin.core.broadcast.Broadcast
import org.gaseumlabs.uhcplugin.world.WorldManager

object LobbyCommands {
	fun build(uhcNode: LiteralArgumentBuilder<CommandSourceStack>) {
		uhcNode.then(
			Commands.literal("ready")
				.requires(CommandUtil::requiresPregame)
				.executes(::execReady)
		)

		uhcNode.then(
			Commands.literal("unready")
				.requires(CommandUtil::requiresPregame)
				.executes(::execUnready)
		)

		uhcNode.then(
			Commands.literal("forceready")
				.requires(CommandUtil::requiresPregameOp)
				.then(
					CommandUtil.createPlayerArgument("player", "Force ready player")
						.executes(::execForceReady)
				)
		)

		uhcNode.then(
			Commands.literal("forceunready")
				.requires(CommandUtil::requiresPregameOp)
				.then(
					CommandUtil.createPlayerArgument("player", "Force ready player")
						.executes(::execForceUnready)
				)
		)

		uhcNode.then(
			Commands.literal("minreadyplayers")
				.requires(CommandUtil::requiresPregameOp)
				.then(
					Commands.literal("set").then(
						Commands.argument("value", IntegerArgumentType.integer(1, 16))
							.executes(::execSetMinReadyPlayers)
					)
				)
		)

		uhcNode.then(
			Commands.literal("readyall")
				.requires(CommandUtil::requiresPregameOp)
				.executes(::execReadyAll)
		)

		uhcNode.then(
			Commands.literal("lobby")
				.executes(::execLobby)
		)
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
		val player = CommandUtil.getSenderPlayer(context) ?: return CommandUtil.notPlayerError(context)

		val game = UHC.getGame()
		val playerData = game?.playerDatas?.get(player)

		if (playerData != null && playerData.isActive) return CommandUtil.error(context, "You are currently playing!")

		PlayerManip.resetPlayer(
			player,
			GameMode.ADVENTURE,
			20.0,
			WorldManager.getWorld(WorldManager.UHCWorldType.LOBBY).spawnLocation
		)

		return Command.SINGLE_SUCCESS
	}
}
