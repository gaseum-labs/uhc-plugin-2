package org.gaseumlabs.uhcplugin.core.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemType
import org.gaseumlabs.uhcplugin.help.UHCAdvancementManager
import org.gaseumlabs.uhcplugin.help.UHCAdvancements

@Suppress("EXPERIMENTAL_API_USAGE")
object DebugCommands {
	fun build(): LiteralCommandNode<CommandSourceStack> {
		val advancementNode = Commands.literal("advancement")

		advancementNode.then(
			Commands.literal("register").executes {
				UHCAdvancementManager.registerRoot(UHCAdvancements.UHC)
				return@executes Command.SINGLE_SUCCESS
			}
		)
		advancementNode.then(
			Commands.literal("unregister").executes {
				UHCAdvancementManager.unregister()
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

		val uhcNode = Commands.literal("uhc")

		uhcNode.then(debugNode)

		return uhcNode.build()
	}
}