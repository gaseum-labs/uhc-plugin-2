package org.gaseumlabs.uhcplugin.core.team

import net.kyori.adventure.text.Component
import org.bukkit.scoreboard.Team
import org.gaseumlabs.uhcplugin.core.playerData.PlayerData
import java.util.*

class UHCTeam(
	val uuid: UUID,
	val memberUUIDs: ArrayList<UUID>,
	val name: String,
	val color: TeamColor,
	val team: Team,
) {
	val size: Int
		get() = memberUUIDs.size

	val members = ArrayList<PlayerData>()

	fun nameComponent() = Component.text(name, color.textColor)
}
