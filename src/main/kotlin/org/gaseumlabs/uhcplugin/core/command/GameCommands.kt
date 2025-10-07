package org.gaseumlabs.uhcplugin.core.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.LivingEntity
import org.gaseumlabs.uhcplugin.core.*
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import org.gaseumlabs.uhcplugin.core.game.PostGame
import org.gaseumlabs.uhcplugin.core.playerData.OfflineZombie
import org.gaseumlabs.uhcplugin.core.playerData.PlayerCapture
import org.gaseumlabs.uhcplugin.core.playerData.PlayerData

object GameCommands {
	fun build(uhcNode: LiteralArgumentBuilder<CommandSourceStack>) {
		uhcNode.then(
			Commands.literal("spectate")
				.requires(CommandUtil.makeRequires(RequiresFlag.GAME))
				.executes(::execSpectate)
		)

		uhcNode.then(
			Commands.literal("forfeit")
				.requires(CommandUtil.makeRequires(RequiresFlag.ACTIVE_GAME))
				.executes(::execForfeit)
		)

		uhcNode.then(
			Commands.literal("reset")
				.requires(CommandUtil.makeRequires(RequiresFlag.OP, RequiresFlag.GAME))
				.executes(::execReset)
		)

		uhcNode.then(
			Commands.literal("end")
				.requires(CommandUtil.makeRequires(RequiresFlag.OP, RequiresFlag.ACTIVE_GAME))
				.executes(::execEnd)
		)

		uhcNode.then(
			Commands.literal("addlate")
				.requires(CommandUtil.makeRequires(RequiresFlag.OP, RequiresFlag.ACTIVE_GAME))
				.then(
					CommandUtil.createPlayerArgument("player", "Add late player")
						.executes(::execAddLate)
				)
		)

		uhcNode.then(
			Commands.literal("phase")
				.requires(CommandUtil.makeRequires(RequiresFlag.OP, RequiresFlag.ACTIVE_GAME))
				.then(
					Commands.literal("start").then(
						Commands.literal("shrink").executes(::execPhaseStartShrink)
					).then(
						Commands.literal("endgame").executes(::execPhaseStartEndgame)
					)
				)
		)
	}

	fun execReset(context: CommandContext<CommandSourceStack>): Int {
		val game = UHC.game() ?: return CommandUtil.error(context, "Game is not going")

		UHC.gameToPreGame(game)

		CommandUtil.successMessage(context, "Game reset")

		return Command.SINGLE_SUCCESS
	}

	fun execEnd(context: CommandContext<CommandSourceStack>): Int {
		val activeGame = UHC.activeGame() ?: return CommandUtil.error(context, "Game is not going")

		UHC.activeGameToPostGame(activeGame, null)

		CommandUtil.successMessage(context, "Game reset")

		return Command.SINGLE_SUCCESS
	}

	fun execSpectate(context: CommandContext<CommandSourceStack>): Int {
		val player = CommandUtil.getSenderPlayer(context) ?: return CommandUtil.notPlayerError(context)
		val game = UHC.game() ?: return CommandUtil.error(context, "No ongoing game")

		val location = when (game) {
			is ActiveGame -> {
				if (game.playerDatas.getActive(player) != null)
					return CommandUtil.error(context, "You are still playing")

				game.playerDatas.active.map { playerData ->
					playerData.getEntity()?.location ?: playerData.offlineRecord.spectateLocation
				}.randomOrNull() ?: game.gameWorld.spawnLocation
			}

			is PostGame -> {
				game.gameWorld.players.filter { player -> player.gameMode !== GameMode.SPECTATOR }
					.randomOrNull()?.location ?: game.gameWorld.spawnLocation
			}
		}

		PlayerManip.makeSpectator(player, location)

		return Command.SINGLE_SUCCESS
	}

	fun execForfeit(context: CommandContext<CommandSourceStack>): Int {
		val player = CommandUtil.getSenderPlayer(context) ?: return CommandUtil.notPlayerError(context)
		val activeGame = UHC.activeGame() ?: return CommandUtil.error(context, "No ongoing game")

		val playerData = activeGame.playerDatas.getActive(player)
			?: return CommandUtil.error(context, "You are not in the game")

		UHC.onPlayerDeath(activeGame, playerData, player.location, null, Death.getForfeitDeathMessage(player), true)
		Death.dropPlayer(player)

		return Command.SINGLE_SUCCESS
	}

	fun execAddLate(context: CommandContext<CommandSourceStack>): Int {
		val activeGame = UHC.activeGame() ?: return CommandUtil.error(context, "No ongoing game")
		val playerName = context.getArgument("player", String::class.java)

		val player = Bukkit.getOfflinePlayerIfCached(playerName) ?: return CommandUtil.error(
			context,
			"The player does not exist"
		)

		val existingPlayerData = activeGame.playerDatas.get(player)

		data class AddLateResult(val playerData: PlayerData, val spawnBuddy: LivingEntity?)

		val (playerData, spawnBuddy) = when {
			existingPlayerData == null -> {
				val (team, spawnBuddy) = activeGame.teams.findTeamInNeedOr(activeGame) {
					activeGame.teams.createTeam(listOf(player.uniqueId))
				}
				AddLateResult(
					activeGame.playerDatas.add(PlayerData.createInitial(player.uniqueId, team)),
					spawnBuddy,
				)
			}

			!existingPlayerData.isActive -> {
				AddLateResult(
					existingPlayerData,
					activeGame.teams.getSpawnBuddy(activeGame, existingPlayerData.team)
				)
			}

			else -> {
				return CommandUtil.error(context, "${player.name} is already in the game")
			}
		}

		playerData.reset()

		val location = spawnBuddy?.location ?: PlayerSpreader.getSingleLocation(
			activeGame,
			player.uniqueId,
			activeGame.gameWorld,
			UHCBorder.getBorderRadius(activeGame.gameWorld.worldBorder),
			PlayerSpreader.CONFIG_DEFAULT
		) ?: activeGame.gameWorld.spawnLocation

		playerData.executeAction { player ->
			PlayerManip.resetPlayer(player, GameMode.SURVIVAL, playerData.maxHealth, location)
		}.onNoZombie {
			OfflineZombie.spawn(
				player.uniqueId,
				PlayerCapture.createInitial(location, playerData.maxHealth)
			)
		}

		CommandUtil.successMessage(context, "${player.name} added late to the game")

		return Command.SINGLE_SUCCESS
	}

	fun execPhaseStartShrink(context: CommandContext<CommandSourceStack>): Int {
		val activeGame = UHC.activeGame() ?: return CommandUtil.error(context, "Game is not going")

		activeGame.timer = activeGame.SHRINK_START_TIME

		CommandUtil.successMessage(context, "Started phase shrink")

		return Command.SINGLE_SUCCESS
	}

	fun execPhaseStartEndgame(context: CommandContext<CommandSourceStack>): Int {
		val activeGame = UHC.activeGame() ?: return CommandUtil.error(context, "Game is not going")

		activeGame.timer = activeGame.ENDGAME_START_TIME

		CommandUtil.successMessage(context, "Started phase endgame")

		return Command.SINGLE_SUCCESS
	}
}
