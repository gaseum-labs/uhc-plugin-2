package org.gaseumlabs.uhcplugin.core.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemType
import org.gaseumlabs.uhcplugin.help.AdvancementRegistry
import org.gaseumlabs.uhcplugin.help.UHCAdvancements

@Suppress("EXPERIMENTAL_API_USAGE")
object DebugCommands {
	fun build(uhcNode: LiteralArgumentBuilder<CommandSourceStack>) {
		val advancementNode = Commands.literal("advancement")

		advancementNode.then(
			Commands.literal("register").executes {
				AdvancementRegistry.registerRoot(UHCAdvancements.UHC)
				return@executes Command.SINGLE_SUCCESS
			}
		)

		val serializeNode = Commands.literal("serialize")

		serializeNode.executes {
			val itemStack = ItemType.IRON_SWORD.createItemStack { meta ->
				meta.addEnchant(Enchantment.SHARPNESS, 2, true)
			}

			val serialized = itemStack.serialize()

			return@executes Command.SINGLE_SUCCESS
		}

		val debugNode = Commands.literal("debug")

		debugNode.then(advancementNode)
		debugNode.then(serializeNode)

		uhcNode.then(debugNode)
	}
}