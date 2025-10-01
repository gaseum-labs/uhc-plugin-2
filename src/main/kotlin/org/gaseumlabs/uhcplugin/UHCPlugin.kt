package org.gaseumlabs.uhcplugin

import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.UnsafeValues
import org.bukkit.plugin.java.JavaPlugin
import org.gaseumlabs.uhcplugin.core.Display
import org.gaseumlabs.uhcplugin.core.GameEvents
import org.gaseumlabs.uhcplugin.core.Login
import org.gaseumlabs.uhcplugin.core.UHC
import org.gaseumlabs.uhcplugin.core.playerData.OfflineZombie
import org.gaseumlabs.uhcplugin.discord.GameRunnerBot
import org.gaseumlabs.uhcplugin.fix.BrewFix
import org.gaseumlabs.uhcplugin.fix.PearlFix
import org.gaseumlabs.uhcplugin.fix.PortalFix
import org.gaseumlabs.uhcplugin.help.AdvancementEvents
import org.gaseumlabs.uhcplugin.help.AdvancementRegistry
import org.gaseumlabs.uhcplugin.help.UHCAdvancements

class UHCPlugin : JavaPlugin() {
	override fun onEnable() {
		self = this

		println("Enabling UHC Plugin")

		WorldGen.configureCarvers()

		Bukkit.getPluginManager().registerEvents(Login(), this)
		Bukkit.getPluginManager().registerEvents(OfflineZombie.Events(), this)
		Bukkit.getPluginManager().registerEvents(Display.Events(), this)
		Bukkit.getPluginManager().registerEvents(GameEvents(), this)
		Bukkit.getPluginManager().registerEvents(PearlFix(), this)
		Bukkit.getPluginManager().registerEvents(PortalFix(), this)
		Bukkit.getPluginManager().registerEvents(BrewFix(), this)
		Bukkit.getPluginManager().registerEvents(AdvancementEvents(), this)

		Bukkit.getScheduler().scheduleSyncDelayedTask(this, UHC::init)

		AdvancementRegistry.registerRoot(UHCAdvancements.UHC)

		GameRunnerBot.setup()
	}

	companion object {
		lateinit var self: UHCPlugin
			private set

		fun key(key: String) = NamespacedKey(self, key)

		val unsafe: UnsafeValues
			get() = Bukkit.getUnsafe()
	}
}
