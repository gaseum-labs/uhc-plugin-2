package org.gaseumlabs.uhcplugin.help

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.gaseumlabs.uhcplugin.UHCPlugin

object UHCAdvancementManager {
	@SuppressWarnings("deprecation")
	fun registerRoot(root: UHCAdvancement) {
		val navigatorStack = ArrayList<UHCAdvancement>()
		navigatorStack.add(root)

		val allAdvancements = ArrayList<UHCAdvancement>()

		var current: UHCAdvancement?
		while (true) {
			current = navigatorStack.removeFirstOrNull() ?: break
			navigatorStack.addAll(current.children)
			allAdvancements.add(current)
		}

		for (advancement in allAdvancements) {
			if (Bukkit.getServer().getAdvancement(advancement.key) != null) {
				UHCPlugin.unsafe.removeAdvancement(advancement.key)
			}
		}
		Bukkit.getServer().reloadData()

		for (advancement in allAdvancements) {
			UHCPlugin.unsafe.loadAdvancement(advancement.key, advancement.serialize())
		}
	}

	fun unregister() {
		val uhcAdvKey = NamespacedKey(UHCPlugin.Companion.self, "adv_aaa")

		if (Bukkit.getServer().getAdvancement(uhcAdvKey) != null) {
			UHCPlugin.unsafe.removeAdvancement(uhcAdvKey)
			Bukkit.getServer().reloadData()
		}
	}

	fun test() {
		Material.APPLE.key
	}
}