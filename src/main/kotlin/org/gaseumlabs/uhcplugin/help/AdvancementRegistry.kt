package org.gaseumlabs.uhcplugin.help

import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.advancement.Advancement
import org.gaseumlabs.uhcplugin.UHCPlugin

object AdvancementRegistry {
	private val advancementMap = HashMap<UHCAdvancement, Advancement>()
	private val keyMap = HashMap<NamespacedKey, Advancement>()

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

		for (uhc in allAdvancements) {
			val minecraft = UHCPlugin.unsafe.loadAdvancement(uhc.key, uhc.serialize())

			advancementMap[uhc] = minecraft
			keyMap[uhc.key] = minecraft
		}
	}

	fun getMinecraft(key: NamespacedKey): Advancement? {
		return keyMap[key]
	}

	fun getMinecraft(uhc: UHCAdvancement): Advancement {
		return advancementMap[uhc] ?: throw Exception("UHC Advancement not registered")
	}
}