package org.gaseumlabs.uhcplugin.core.team

import net.kyori.adventure.text.Component
import org.bukkit.scoreboard.Team
import java.util.*

open class PreTeam(
	val uuid: UUID,
	val memberUUIDs: HashSet<UUID>,
	val name: String,
	val color: TeamColor,
	val team: Team,
) {
	val size: Int
		get() = memberUUIDs.size

	fun nameComponent() = Component.text(name, color.textColor)
}

typealias TeamConstructor<T> = (uuid: UUID, memberUUIDs: HashSet<UUID>, name: String, color: TeamColor, team: Team) -> T
