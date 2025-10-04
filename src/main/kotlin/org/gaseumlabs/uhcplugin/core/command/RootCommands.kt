package org.gaseumlabs.uhcplugin.core.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.gaseumlabs.uhcplugin.core.UHC

object RootCommands {
	fun build(): List<LiteralArgumentBuilder<CommandSourceStack>> {
		return listOf(
			Commands.literal("sharecoords")
				.requires(CommandUtil.makeRequires(RequiresFlag.ACTIVE_GAME))
				.executes(::execShareCoords)
		)
	}

	private fun execShareCoords(context: CommandContext<CommandSourceStack>): Int {
		val player = CommandUtil.getSenderPlayer(context) ?: return CommandUtil.notPlayerError(context)

		val activeGame = UHC.activeGame() ?: return CommandUtil.error(context, "No game going")
		val playerData =
			activeGame.playerDatas.getActive(player) ?: return CommandUtil.error(context, "You are not playing")

		val block = player.location.block

		val team = playerData.team
		team.members.forEach { playerData ->
			playerData.getPlayer()?.sendMessage(
				Component.text("${player.name} is located at ", team.color.textColor).append(
					Component.text(
						"(${block.x}, ${block.y}, ${block.z})", team.color.textColor,
						TextDecoration.BOLD
					)
				)
			)
		}

		return Command.SINGLE_SUCCESS
	}
}
