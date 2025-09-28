package org.gaseumlabs.uhcplugin.core.team

import net.kyori.adventure.text.format.NamedTextColor
import java.util.*

class Team(
	val uuid: UUID,
	var name: String,
	var color: NamedTextColor,
	val members: Set<UUID>,
) {

}
