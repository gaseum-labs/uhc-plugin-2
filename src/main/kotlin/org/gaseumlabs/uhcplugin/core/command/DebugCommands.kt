package org.gaseumlabs.uhcplugin.core.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemType
import org.gaseumlabs.uhcplugin.core.PlayerSpreader
import org.gaseumlabs.uhcplugin.core.UHC
import org.gaseumlabs.uhcplugin.core.UHCBorder
import org.gaseumlabs.uhcplugin.core.game.StartGameMode
import org.gaseumlabs.uhcplugin.help.AdvancementRegistry
import org.gaseumlabs.uhcplugin.help.UHCAdvancements
import org.gaseumlabs.uhcplugin.world.SurfaceFinder
import org.gaseumlabs.uhcplugin.world.YFinder

object DebugCommands {
	fun build(uhcNode: LiteralArgumentBuilder<CommandSourceStack>) {
		val advancementNode = Commands.literal("advancement")

		advancementNode.then(
			Commands.literal("register")
				.requires(CommandUtil.makeRequires(RequiresFlag.OP)).executes {
					AdvancementRegistry.registerRoot(UHCAdvancements.UHC)
					return@executes Command.SINGLE_SUCCESS
				}
		)

		val serializeNode = Commands.literal("serialize")

		serializeNode.requires(CommandUtil.makeRequires(RequiresFlag.OP)).executes {
			val itemStack = ItemType.IRON_SWORD.createItemStack { meta ->
				meta.addEnchant(Enchantment.SHARPNESS, 2, true)
			}

			val serialized = itemStack.serialize()

			return@executes Command.SINGLE_SUCCESS
		}

		val startNode = Commands.literal("start")
		startNode.requires(CommandUtil.makeRequires(RequiresFlag.OP)).executes { context ->
			val preGame = UHC.preGame() ?: return@executes CommandUtil.error(context, "Game already going")

			preGame.startGameMode = StartGameMode.HOST
			preGame.teams.fillRandomTeams(Bukkit.getOnlinePlayers().map { it.uniqueId }, 1)
			UHC.preGameToActiveGame(preGame)

			return@executes Command.SINGLE_SUCCESS
		}

		val spreadNode = Commands.literal("spread")
		spreadNode.requires(CommandUtil.makeRequires(RequiresFlag.OP)).executes(::execSpread)

		val findSurfaceNode = Commands.literal("findsurface")
		findSurfaceNode.requires(CommandUtil.makeRequires(RequiresFlag.OP)).executes(::execFindSurface)

		val blockInfoNode = Commands.literal("blockinfo")
		blockInfoNode.requires(CommandUtil.makeRequires(RequiresFlag.OP)).executes(::execBlockInfo)

		val debugNode = Commands.literal("debug")

		debugNode.then(blockInfoNode)
		debugNode.then(findSurfaceNode)
		debugNode.then(spreadNode)
		debugNode.then(advancementNode)
		debugNode.then(serializeNode)
		debugNode.then(startNode)

		uhcNode.then(debugNode)
	}

	fun execSpread(context: CommandContext<CommandSourceStack>): Int {
		val activeGame = UHC.activeGame() ?: return CommandUtil.error(context, "No Game")

		val player = CommandUtil.getSenderPlayer(context) ?: return CommandUtil.notPlayerError(context)

		val location = PlayerSpreader.getSingleLocation(
			activeGame, player.uniqueId, activeGame.gameWorld,
			UHCBorder.getBorderRadius(activeGame.gameWorld.worldBorder), PlayerSpreader.CONFIG_DEFAULT
		)

		player.teleport(location)

		return Command.SINGLE_SUCCESS
	}

	fun execBlockInfo(context: CommandContext<CommandSourceStack>): Int {
		val player = CommandUtil.getSenderPlayer(context) ?: return CommandUtil.notPlayerError(context)

		val block = player.getTargetBlockExact(100) ?: return CommandUtil.error(context, "no block")

		CommandUtil.successMessage(context, "passable: ${block.isPassable}")
		CommandUtil.successMessage(context, "material: ${block.type}")
		CommandUtil.successMessage(context, "isTree: ${YFinder.isTreeBlock(block)}")
		CommandUtil.successMessage(context, "isSurface: ${YFinder.isSurfaceBlock(block)}")

		return Command.SINGLE_SUCCESS
	}

	fun execFindSurface(context: CommandContext<CommandSourceStack>): Int {
		val player = CommandUtil.getSenderPlayer(context) ?: return CommandUtil.notPlayerError(context)

		val block = player.location.block

		val surfaceBlocks = SurfaceFinder.getSurfaceBlocks(block.world, block.x, block.z, 7)

		surfaceBlocks.forEach { block ->
			if (block.liquid && block.tree) {
				block.block.setType(Material.FIRE_CORAL_BLOCK, false)
			} else if (block.liquid) {
				block.block.setType(Material.PRISMARINE, false)
			} else if (block.tree) {
				block.block.setType(Material.RESIN_BLOCK, false)
			} else {
				block.block.setType(Material.REDSTONE_BLOCK, false)
			}
		}

		CommandUtil.successMessage(context, "found surface")

		return Command.SINGLE_SUCCESS
	}
}