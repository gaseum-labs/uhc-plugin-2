package org.gaseumlabs.uhcplugin.core.broadcast

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

object MSG {
	fun game(text: String) = Component.text(text, TextColor.color(0xfc9d03))
	fun gameBold(text: String) = Component.text(text, TextColor.color(0xfc9d03), TextDecoration.BOLD)

	fun success(text: String): Component = Component.text(text, TextColor.color(0x1cc916))

	fun error(text: String): Component = Component.text(text, TextColor.color(0xe61014))

	fun help(text: String): Component {
		return Component.text().append(Component.text()
			.content("2HC HELP: ")
			.color(TextColor.color(0x5368f5))
			.decoration(TextDecoration.BOLD, true))
			.append(
				Component.text()
					.content(text)
					.color(TextColor.color(0x5368f5))
					.build()
			)
			.build()
	}
}