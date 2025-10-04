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
import org.gaseumlabs.uhcplugin.core.broadcast.MSG
import org.gaseumlabs.uhcplugin.world.WorldManager

object LobbyCommands {
	fun build(uhcNode: LiteralArgumentBuilder<CommandSourceStack>) {
		uhcNode.then(
			Commands.literal("lobby")
				.executes(::execLobby)
		)

		uhcNode.then(
			Commands.literal("ready")
				.requires(CommandUtil.makeRequires(RequiresFlag.PRE_GAME))
				.executes(::execReady)
		)

		uhcNode.then(
			Commands.literal("unready")
				.requires(CommandUtil.makeRequires(RequiresFlag.PRE_GAME))
				.executes(::execUnready)
		)

		uhcNode.then(
			Commands.literal("forceready")
				.requires(CommandUtil.makeRequires(RequiresFlag.OP, RequiresFlag.PRE_GAME))
				.then(
					CommandUtil.createPlayerArgument("player", "Force ready player")
						.executes(::execForceReady)
				)
		)

		uhcNode.then(
			Commands.literal("forceunready")
				.requires(CommandUtil.makeRequires(RequiresFlag.OP, RequiresFlag.PRE_GAME))
				.then(
					CommandUtil.createPlayerArgument("player", "Force ready player")
						.executes(::execForceUnready)
				)
		)

		uhcNode.then(
			Commands.literal("minreadyplayers")
				.requires(CommandUtil.makeRequires(RequiresFlag.OP, RequiresFlag.PRE_GAME))
				.then(
					Commands.literal("set").then(
						Commands.argument("value", IntegerArgumentType.integer(1, 16))
							.executes(::execSetMinReadyPlayers)
					)
				)
		)

		uhcNode.then(
			Commands.literal("readyall")
				.requires(CommandUtil.makeRequires(RequiresFlag.OP, RequiresFlag.PRE_GAME))
				.executes(::execReadyAll)
		)

		uhcNode.then(
			Commands.literal("host")
				.requires(CommandUtil.makeRequires(RequiresFlag.OP, RequiresFlag.PRE_GAME))
				.then(
					Commands.literal("enable")
						.executes(::execHostEnable)
				).then(
					Commands.literal("disable")
						.executes(::execHostDisable)
				)
		)

		uhcNode.then(
			Commands.literal("team")
				.requires(CommandUtil.makeRequires(RequiresFlag.OP, RequiresFlag.PRE_GAME))
				.then(
					Commands.literal("clear")
						.executes(::execTeamClear)
				)
				.then(
					Commands.literal("random")
						.executes(::execTeamRandom)
				)
				.then(
					Commands.literal("create")
						.then(
							CommandUtil.createPlayerArgument("player1", "player to add")
								.executes(::execTeamCreate1)
								.then(
									CommandUtil.createPlayerArgument("player2", "another player to add")
										.executes(::execTeamCreate2)
								)
						)
				)
				.then(
					Commands.literal("destroy")
						.then(
							CommandUtil.createPlayerArgument("player", "team player")
								.executes(::execTeamDestroy)
						)

				)
				.then(
					Commands.literal("move")
						.then(
							CommandUtil.createPlayerArgument("player", "player to move")
								.then(
									CommandUtil.createPlayerArgument("teamPlayer", "player with team to move to")
										.executes(::execTeamMove)
								)
						)
				)
				.then(
					Commands.literal("leave")
						.then(
							CommandUtil.createPlayerArgument("player", "player to move")
								.executes(::execTeamLeave)
						)
				)
		)
	}

	private fun broadcastReady(player: OfflinePlayer) {
		Broadcast.broadcast(
			player,
			MSG.success("You are now ready for the game"),
			MSG.game("${player.name} is now ready")
		)
	}

	private fun broadcastUnready(player: OfflinePlayer) {
		Broadcast.broadcast(
			player,
			MSG.success("You are no longer ready for the game"),
			MSG.game("${player.name} is no longer ready")
		)
	}

	private fun execReady(context: CommandContext<CommandSourceStack>): Int {
		val player = CommandUtil.getSenderPlayer(context) ?: return CommandUtil.notPlayerError(context)
		val pregame = UHC.preGame() ?: return CommandUtil.error(context, "Game has already started")

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
		val pregame = UHC.preGame() ?: return CommandUtil.error(context, "Game has already started")

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

		val pregame = UHC.preGame() ?: return CommandUtil.error(context, "Game has already started")

		val forcePlayer = Bukkit.getOfflinePlayerIfCached(forcePlayerName)
			?: return CommandUtil.error(context, "That player does not exist")

		pregame.makePlayerReady(forcePlayer.uniqueId)

		broadcastReady(forcePlayer)

		return Command.SINGLE_SUCCESS
	}

	private fun execForceUnready(context: CommandContext<CommandSourceStack>): Int {
		val forcePlayerName = context.getArgument("player", String::class.java)

		val pregame = UHC.preGame() ?: return CommandUtil.error(context, "Game has already started")

		val forcePlayer = Bukkit.getOfflinePlayerIfCached(forcePlayerName)
			?: return CommandUtil.error(context, "That player does not exist")

		pregame.makePlayerUnready(forcePlayer.uniqueId)

		broadcastUnready(forcePlayer)

		return Command.SINGLE_SUCCESS
	}

	private fun execSetMinReadyPlayers(context: CommandContext<CommandSourceStack>): Int {
		val value = context.getArgument("value", Int::class.java)

		val pregame = UHC.preGame() ?: return CommandUtil.error(context, "Game has already started")

		pregame.minReadyPlayers = value

		CommandUtil.successMessage(context, "Min ready players set to $value")

		return Command.SINGLE_SUCCESS
	}

	private fun execReadyAll(context: CommandContext<CommandSourceStack>): Int {
		val pregame = UHC.preGame() ?: return CommandUtil.error(context, "Game has already started")

		Bukkit.getOnlinePlayers().forEach { player ->
			if (pregame.makePlayerReady(player.uniqueId)) {
				broadcastReady(player)
			}
		}

		return Command.SINGLE_SUCCESS
	}

	private fun execLobby(context: CommandContext<CommandSourceStack>): Int {
		val player = CommandUtil.getSenderPlayer(context) ?: return CommandUtil.notPlayerError(context)

		if (UHC.activeGame()?.playerDatas?.getActive(player) != null) {
			return CommandUtil.error(context, "You are currently playing!")
		}

		PlayerManip.resetPlayer(
			player,
			GameMode.ADVENTURE,
			20.0,
			WorldManager.lobby.spawnLocation
		)

		return Command.SINGLE_SUCCESS
	}

	private fun execHostEnable(context: CommandContext<CommandSourceStack>): Int {
		val preGame = UHC.preGame() ?: return CommandUtil.error(context, "Game already going")
		preGame.hostMode = true
		CommandUtil.successMessage(context, "Enabled host mode")
		return Command.SINGLE_SUCCESS
	}

	private fun execHostDisable(context: CommandContext<CommandSourceStack>): Int {
		val preGame = UHC.preGame() ?: return CommandUtil.error(context, "Game already going")
		preGame.hostMode = false
		CommandUtil.successMessage(context, "Disabled host mode")
		return Command.SINGLE_SUCCESS
	}

	private fun execTeamClear(context: CommandContext<CommandSourceStack>): Int {
		val preGame = UHC.preGame() ?: return CommandUtil.error(context, "Game already going")

		preGame.teams.clearTeams()

		CommandUtil.successMessage(context, "Teams cleared")

		return Command.SINGLE_SUCCESS
	}

	private fun execTeamRandom(context: CommandContext<CommandSourceStack>): Int {
		val preGame = UHC.preGame() ?: return CommandUtil.error(context, "Game already going")

		preGame.teams.fillRandomTeams(Bukkit.getOnlinePlayers().map { player -> player.uniqueId }, 2)

		CommandUtil.successMessage(context, "Filled random teams")

		return Command.SINGLE_SUCCESS
	}

	private fun execTeamCreate1(context: CommandContext<CommandSourceStack>): Int {
		val preGame = UHC.preGame() ?: return CommandUtil.error(context, "Game already going")

		val player1Name = context.getArgument("player1", String::class.java)
		val player1 =
			Bukkit.getOfflinePlayerIfCached(player1Name) ?: return CommandUtil.error(context, "Player does not exist")

		preGame.teams.createTeam(listOf(player1.uniqueId))

		CommandUtil.successMessage(context, "Created new team")

		return Command.SINGLE_SUCCESS
	}

	private fun execTeamCreate2(context: CommandContext<CommandSourceStack>): Int {
		val preGame = UHC.preGame() ?: return CommandUtil.error(context, "Game already going")

		val player1Name = context.getArgument("player1", String::class.java)
		val player1 =
			Bukkit.getOfflinePlayerIfCached(player1Name) ?: return CommandUtil.error(context, "Player does not exist")

		val player2Name = context.getArgument("player2", String::class.java)
		val player2 =
			Bukkit.getOfflinePlayerIfCached(player1Name) ?: return CommandUtil.error(context, "Player does not exist")

		preGame.teams.createTeam(listOf(player1.uniqueId, player2.uniqueId))

		CommandUtil.successMessage(context, "Created new team")

		return Command.SINGLE_SUCCESS
	}

	private fun execTeamDestroy(context: CommandContext<CommandSourceStack>): Int {
		val preGame = UHC.preGame() ?: return CommandUtil.error(context, "Game already going")

		val playerName = context.getArgument("player", String::class.java)
		val player =
			Bukkit.getOfflinePlayerIfCached(playerName) ?: return CommandUtil.error(context, "Player does not exist")

		val team = preGame.teams.playersTeam(player) ?: return CommandUtil.error(context, "Player not on a team")

		preGame.teams.destroyTeam(team)

		CommandUtil.successMessage(context, "Destroyed team")

		return Command.SINGLE_SUCCESS
	}

	private fun execTeamMove(context: CommandContext<CommandSourceStack>): Int {
		val preGame = UHC.preGame() ?: return CommandUtil.error(context, "Game already going")

		val playerName = context.getArgument("player", String::class.java)
		val player =
			Bukkit.getOfflinePlayerIfCached(playerName) ?: return CommandUtil.error(context, "Player does not exist")

		val teamPlayerName = context.getArgument("teamPlayer", String::class.java)
		val teamPlayer =
			Bukkit.getOfflinePlayerIfCached(playerName) ?: return CommandUtil.error(context, "Player does not exist")

		val team = preGame.teams.playersTeam(teamPlayer) ?: return CommandUtil.error(context, "Player not on a team")

		preGame.teams.moveToTeam(player.uniqueId, team)

		CommandUtil.successMessage(context, "Moved ${player.name} to ${teamPlayer.name}'s team")

		return Command.SINGLE_SUCCESS
	}

	private fun execTeamLeave(context: CommandContext<CommandSourceStack>): Int {
		val preGame = UHC.preGame() ?: return CommandUtil.error(context, "Game already going")

		val playerName = context.getArgument("player", String::class.java)
		val player =
			Bukkit.getOfflinePlayerIfCached(playerName) ?: return CommandUtil.error(context, "Player does not exist")

		val team = preGame.teams.playersTeam(player) ?: return CommandUtil.error(context, "Player not on a team")

		preGame.teams.removeFromTeam(player.uniqueId)

		CommandUtil.successMessage(context, "Removed ${player.name} from their team")

		return Command.SINGLE_SUCCESS
	}
}
