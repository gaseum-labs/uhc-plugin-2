package org.gaseumlabs.uhcplugin.core

import net.kyori.adventure.text.format.NamedTextColor
import java.util.UUID

class Team(val uid: UUID, val memberUUIDs: ArrayList<UUID>, val name: String, val color: NamedTextColor) {

}