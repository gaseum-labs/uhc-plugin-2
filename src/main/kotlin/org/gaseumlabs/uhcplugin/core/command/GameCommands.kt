package org.gaseumlabs.uhcplugin.core.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.gaseumlabs.uhcplugin.core.CommandUtil
import org.gaseumlabs.uhcplugin.core.Death
import org.gaseumlabs.uhcplugin.core.PlayerManip
import org.gaseumlabs.uhcplugin.core.UHC

object GameCommands {
	fun build(): LiteralCommandNode<CommandSourceStack> {
		val uhcNode = Commands.literal("uhc")

		uhcNode.then(
			Commands.literal("reset")
				.requires { source -> source.sender.isOp && UHC.isGame() }
				.executes(::execReset)
		)

		uhcNode.then(
			Commands.literal("spectate")
				.requires { source -> UHC.isGame() }
				.executes(::execSpectate)
		)

		uhcNode.then(
			Commands.literal("forfeit")
				.requires { source -> UHC.isGame() }
				.executes(::execForfeit)
		)

		return uhcNode.build()
	}

	fun execReset(context: CommandContext<CommandSourceStack>): Int {
		val game = UHC.getGame() ?: return CommandUtil.error(context, "No ongoing game")

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
}