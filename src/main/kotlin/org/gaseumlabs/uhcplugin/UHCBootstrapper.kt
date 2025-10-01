package org.gaseumlabs.uhcplugin;

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.gaseumlabs.uhcplugin.core.command.DebugCommands
import org.gaseumlabs.uhcplugin.core.command.GameCommands
import org.gaseumlabs.uhcplugin.core.command.LobbyCommands

class UHCBootstrapper : PluginBootstrap {
	override fun bootstrap(context: BootstrapContext) {
		context.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
			val uhcNode = Commands.literal("uhc")
			
			LobbyCommands.build(uhcNode)
			GameCommands.build(uhcNode)
			DebugCommands.build(uhcNode)

			val registrar = event.registrar()
			registrar.register(uhcNode.build())
		}
	}
}
