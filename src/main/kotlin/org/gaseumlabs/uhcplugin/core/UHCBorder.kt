package org.gaseumlabs.uhcplugin.core

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldBorder
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.LivingEntity
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import org.gaseumlabs.uhcplugin.core.phase.PhaseType
import org.gaseumlabs.uhcplugin.util.MathUtil
import kotlin.math.absoluteValue
import kotlin.math.ceil

object UHCBorder {
	fun set(world: World, radius: Int) {
		world.worldBorder.setCenter(0.5, 0.5)
		world.worldBorder.size = radius * 2.0 + 1.0
		world.worldBorder.damageAmount = 0.0
		world.worldBorder.damageBuffer = 5.0
	}

	fun shrink(world: World, radius: Int, seconds: Int) {
		world.worldBorder.setCenter(0.5, 0.5)
		world.worldBorder.setSize(radius * 2.0 + 1.0, seconds.toLong())
		world.worldBorder.damageAmount = 0.0
		world.worldBorder.damageBuffer = 5.0
	}

	fun stop(world: World) {
		world.worldBorder.size = world.worldBorder.size
	}

	fun getCurrentRadius(activeGame: ActiveGame): Int {
		val phaseAlong = activeGame.getPhaseAlong()

		return when (phaseAlong.phase.type) {
			PhaseType.GRACE -> activeGame.initialRadius
			PhaseType.SHRINK -> MathUtil.lerp(
				activeGame.finalRadius.toDouble(),
				activeGame.initialRadius.toDouble(),
				phaseAlong.along
			).toInt()

			PhaseType.ENDGAME -> activeGame.finalRadius
		}
	}

	fun getBorderRadius(worldBorder: WorldBorder): Int {
		return ceil((worldBorder.size - 1.0) / 2.0).toInt()
	}

	fun isOutsideBorder(location: Location): Boolean {
		val world = location.world
		val blockRadius = getBorderRadius(world.worldBorder)
		val block = location.block
		return block.x.absoluteValue > blockRadius || block.z.absoluteValue > blockRadius
	}

	fun doDamage(entity: LivingEntity) {
		entity.damage(1.0, DamageSource.builder(DamageType.OUTSIDE_BORDER).build())
	}
}