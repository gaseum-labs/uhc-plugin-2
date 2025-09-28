package org.gaseumlabs.uhcplugin;

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.gaseumlabs.uhcplugin.core.command.DebugCommands
import org.gaseumlabs.uhcplugin.core.command.GameCommands
import org.gaseumlabs.uhcplugin.core.command.LobbyCommands

class UHCBootstrapper : PluginBootstrap {
	override fun bootstrap(context: BootstrapContext) {
		context.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
			val registrar = event.registrar()

			registrar.register(LobbyCommands.build())
			registrar.register(GameCommands.build())
			registrar.register(DebugCommands.build())
		}
	}
}
