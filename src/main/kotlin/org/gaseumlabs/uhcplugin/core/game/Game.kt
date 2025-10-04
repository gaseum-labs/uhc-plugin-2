package org.gaseumlabs.uhcplugin.core.game

import org.bukkit.World
import org.bukkit.entity.Entity
import org.gaseumlabs.uhcplugin.world.UHCWorldType
import org.gaseumlabs.uhcplugin.world.WorldManager

sealed class Game(val gameWorld: World, val netherWorld: World) : Stage {
	open fun destroy() {
		WorldManager.destroyWorld(UHCWorldType.GAME)
		WorldManager.destroyWorld(UHCWorldType.NETHER)
	}

	fun isInWorld(entity: Entity): Boolean {
		return entity.world === gameWorld || entity.world === netherWorld
	}
}
