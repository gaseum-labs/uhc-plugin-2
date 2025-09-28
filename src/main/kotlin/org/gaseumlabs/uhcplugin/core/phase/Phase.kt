package org.gaseumlabs.uhcplugin.core.phase

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.format.TextColor

enum class PhaseType(
	val phaseName: String,
	val barColor: BossBar.Color,
	val textColor: TextColor,
	val canRespawn: Boolean,
	val deathCounts: Boolean,
	val pvp: Boolean,
) {
	GRACE("Grace", BossBar.Color.BLUE, TextColor.color(0x2e9cf0), true, false, false),
	SHRINK("Shrink", BossBar.Color.RED, TextColor.color(0xc41f06), true, true, true),
	ENDGAME("Endgame", BossBar.Color.YELLOW, TextColor.color(0xf0d611), false, true, true)
}

abstract class Phase(val type: PhaseType) {
}

