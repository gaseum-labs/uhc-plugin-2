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
import org.gaseumlabs.uhcplugin.core.playerData.OfflineZombie
import org.gaseumlabs.uhcplugin.core.playerData.PlayerCapture
import org.gaseumlabs.uhcplugin.core.playerData.PlayerData
import org.gaseumlabs.uhcplugin.core.team.Teams

object GameCommands {
	fun build(uhcNode: LiteralArgumentBuilder<CommandSourceStack>) {
		val uhcNode = Commands.literal("uhc")

		uhcNode.then(
			Commands.literal("reset")
				.requires(CommandUtil::requiresPregameOp)
				.executes(::execReset)
		)

		uhcNode.then(
			Commands.literal("spectate")
				.requires(CommandUtil::requiresGame)
				.executes(::execSpectate)
		)

		uhcNode.then(
			Commands.literal("forfeit")
				.requires(CommandUtil::requiresGame)
				.executes(::execForfeit)
		)

		uhcNode.then(
			Commands.literal("addlate")
				.requires(CommandUtil::requiresGameOp)
				.then(
					CommandUtil.createPlayerArgument("player", "Add late player")
						.executes(::execAddLate)
				)
		)
	}

	fun execReset(context: CommandContext<CommandSourceStack>): Int {
		UHC.destroyGame()

		CommandUtil.successMessage(context, "Game reset")

		return Command.SINGLE_SUCCESS
	}

	fun execSpectate(context: CommandContext<CommandSourceStack>): Int {
		val player = CommandUtil.getSenderPlayer(context) ?: return CommandUtil.notPlayerError(context)
		val game = UHC.getGame() ?: return CommandUtil.error(context, "No ongoing game")

		val playerData = game.playerDatas.get(player)
		if (playerData?.isActive == true) return CommandUtil.error(context, "You are still playing")

		val randomPlayerData = game.playerDatas.active.random()
		val location = randomPlayerData.getEntity()?.location ?: randomPlayerData.offlineRecord.spectateLocation
		?: game.gameWorld.spawnLocation

		PlayerManip.makeSpectator(player, location)

		return Command.SINGLE_SUCCESS
	}

	fun execForfeit(context: CommandContext<CommandSourceStack>): Int {
		val player = CommandUtil.getSenderPlayer(context) ?: return CommandUtil.notPlayerError(context)
		val game = UHC.getGame() ?: return CommandUtil.error(context, "No ongoing game")

		val playerData = game.playerDatas.getActive(player)
			?: return CommandUtil.error(context, "You are not in the game")

		UHC.onPlayerDeath(game, playerData, player.location, null, Death.getForfeitDeathMessage(player), true)

		Death.dropPlayer(player)

		return Command.SINGLE_SUCCESS
	}

	fun execAddLate(context: CommandContext<CommandSourceStack>): Int {
		val game = UHC.getActiveGame() ?: return CommandUtil.error(context, "No ongoing game")
		val playerName = context.getArgument("player", String::class.java)

		val player = Bukkit.getOfflinePlayerIfCached(playerName) ?: return CommandUtil.error(
			context,
			"The player does not exist"
		)

		val existingPlayerData = game.playerDatas.get(player)

		data class AddLateResult(val playerData: PlayerData, val spawnBuddy: LivingEntity?)

		val (playerData, spawnBuddy) = when {
			existingPlayerData == null -> {
				val (team, spawnBuddy) = Teams.findTeamInNeedOr(game) { Teams.addTeam(listOf(player.uniqueId)) }
				AddLateResult(
					game.playerDatas.add(PlayerData.createInitial(player.uniqueId, team)),
					spawnBuddy,
				)
			}

			!existingPlayerData.isActive -> {
				AddLateResult(
					existingPlayerData,
					Teams.getSpawnBuddy(game, existingPlayerData.team)
				)
			}

			else -> {
				return CommandUtil.error(context, "${player.name} is already in the game")
			}
		}

		playerData.reset()

		val location = spawnBuddy?.location ?: PlayerSpreader.getSingleLocation(
			player.uniqueId,
			game.gameWorld,
			UHCBorder.getCurrentRadius(game),
			PlayerSpreader.CONFIG_DEFAULT
		) ?: game.gameWorld.spawnLocation

		playerData.executeAction { player ->
			PlayerManip.resetPlayer(player, GameMode.SURVIVAL, playerData.getMaxHealth(), location)
		}.onNoZombie {
			OfflineZombie.spawn(
				player.uniqueId,
				PlayerCapture.createInitial(location, playerData.getMaxHealth())
			)
		}

		CommandUtil.successMessage(context, "${player.name} added late to the game")

		return Command.SINGLE_SUCCESS
	}
}
