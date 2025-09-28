package org.gaseumlabs.uhcplugin.fix

import org.bukkit.Axis
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockType
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPortalEvent
import org.gaseumlabs.uhcplugin.core.UHC
import org.gaseumlabs.uhcplugin.util.MathUtil

class PortalFix : Listener {
	fun onPortal(event: PlayerPortalEvent) {
		event.isCancelled = true

		val game = UHC.getGame() ?: return

		val fromLocation = event.from
		val fromWorld = fromLocation.world

		val toWorld = if (fromWorld === game.gameWorld) game.netherWorld else game.gameWorld

		val fromBlock = fromLocation.block

		val toY = if (fromWorld === game.gameWorld) rangeLerp(fromBlock.y, 0..255, 32..119) else
			rangeLerp(fromBlock.y, 32..119, 0..255)

		val portalOrientation = getPortalOrientation(fromBlock)

		val toBlock = toWorld.getBlockAt(fromBlock.x, toY, fromBlock.z)

		if (toBlock.type !== Material.NETHER_PORTAL) {
			buildNetherPortal(toBlock, portalOrientation)
		}

		event.player.teleport(toBlock.location.add(0.5, 0.0, 0.5))
	}

	fun getPortalOrientation(block: Block): Axis {
		if (block.type !== Material.NETHER_PORTAL) return Axis.X
		if (block.getRelative(-1, 0, 0).type === Material.NETHER_PORTAL || block.getRelative(
				1,
				0,
				0
			).type === Material.NETHER_PORTAL
		) return Axis.X
		return Axis.Z
	}

	fun rangeLerp(input: Int, fromRange: IntRange, toRange: IntRange): Int {
		val fromValue = input.coerceIn(fromRange)
		val along = MathUtil.invLerp(fromRange.first, fromRange.last, fromValue)
		val toValue = MathUtil.lerp(toRange.first, toRange.last, along)
		return toValue
	}

	fun buildNetherPortal(block: Block, axis: Axis) {
		val portalData = BlockType.NETHER_PORTAL.createBlockData { orientable -> orientable.axis = axis }

		for (y in -1..3) {
			for (main in -1..2) {
				val back = if (axis === Axis.X) block.getRelative(main, y, -1) else block.getRelative(-1, y, main)
				val mid = if (axis === Axis.X) block.getRelative(main, y, 0) else block.getRelative(0, y, main)
				val front = if (axis === Axis.X) block.getRelative(main, y, 1) else block.getRelative(1, y, main)

				if (y == 0) {
					mid.setType(Material.OBSIDIAN, false)
					if (main in 0..1) {
						if (back.isPassable) back.setType(Material.OBSIDIAN, false)
						if (front.isPassable) back.setType(Material.OBSIDIAN, false)
					}
				} else {
					if (main in 0..1 && y < 3) {
						mid.setBlockData(portalData, false)
					} else {
						mid.setType(Material.OBSIDIAN, false)
					}
					back.setType(Material.AIR, false)
					front.setType(Material.AIR, false)
				}

			}
		}
	}
}