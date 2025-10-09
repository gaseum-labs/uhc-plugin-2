package org.gaseumlabs.uhcplugin.fix

import org.bukkit.Axis
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPortalEvent
import org.gaseumlabs.uhcplugin.core.UHC
import org.gaseumlabs.uhcplugin.core.UHCBorder
import org.gaseumlabs.uhcplugin.core.broadcast.MSG
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import org.gaseumlabs.uhcplugin.core.phase.EndgamePhase
import org.gaseumlabs.uhcplugin.util.MathUtil

class PortalFix : Listener {
	@EventHandler
	fun onPortal(event: PlayerPortalEvent) {
		event.isCancelled = true

		val game = UHC.game() ?: return

		val fromLocation = event.from
		val fromWorld = fromLocation.world
		val fromBlock = fromLocation.block

		val isCancelledUHC = game is ActiveGame && game.getPhase() is EndgamePhase
		if (isCancelledUHC) {
			event.player.sendMessage(MSG.game("The Nether is closed"))
			return destroyPortal(fromBlock)
		}

		val toWorld = if (fromWorld === game.gameWorld) game.netherWorld else game.gameWorld

		val portalOrientation = getPortalOrientation(fromBlock)

		val toBlock = getPortalExitLocation(toWorld, fromBlock)

		if (toBlock.type !== Material.NETHER_PORTAL) {
			buildNetherPortal(toBlock, portalOrientation)
		}

		event.player.teleport(toBlock.location.add(0.5, 0.0, 0.5))
	}

	fun destroyPortal(block: Block) {
		block.setType(Material.AIR, true)
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

	companion object {
		data class Coord(val x: Int, val z: Int)

		val OVERWORLD_Y_RANGE = 0..255
		val NETHER_Y_RANGE = 32..119

		fun rangeLerp(input: Int, fromRange: IntRange, toRange: IntRange): Int {
			val fromValue = input.coerceIn(fromRange)
			val along = MathUtil.invLerp(fromRange.first, fromRange.last, fromValue)
			val toValue = MathUtil.lerp(toRange.first, toRange.last, along)
			return toValue
		}

		fun getWorldYRange(world: World): IntRange {
			return if (world.environment === World.Environment.NORMAL) OVERWORLD_Y_RANGE
			else NETHER_Y_RANGE
		}

		fun borderLimitXZ(world: World, x: Int, z: Int): Coord {
			val limit = UHCBorder.getBorderRadius(world.worldBorder)
			val BUFFER = 7
			return Coord(x.coerceIn(-limit + BUFFER, limit - BUFFER), z.coerceIn(-limit + BUFFER, limit - BUFFER))
		}

		fun getPortalExitLocation(toWorld: World, fromBlock: Block): Block {
			val fromWorld = fromBlock.world

			val toY = rangeLerp(fromBlock.y, getWorldYRange(fromWorld), getWorldYRange(toWorld))

			val (exitX, exitZ) = borderLimitXZ(toWorld, fromBlock.x, fromBlock.z)

			return toWorld.getBlockAt(exitX, toY, exitZ)
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
}