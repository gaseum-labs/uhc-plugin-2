package org.gaseumlabs.uhcplugin.core

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.gaseumlabs.uhcplugin.core.phase.EndgamePhase
import org.gaseumlabs.uhcplugin.core.phase.PhaseType
import org.gaseumlabs.uhcplugin.core.timer.TickTime
import org.gaseumlabs.uhcplugin.util.MathUtil
import org.gaseumlabs.uhcplugin.world.WorldManager

object Display {
	val playerToBar = HashMap<Player, BossBar>()

	data class DisplayTemplate(
		val name: Component,
		val progress: Double,
		val color: BossBar.Color,
		val overlay: BossBar.Overlay,
		val title: Title?,
	) {
		fun floatProgress() = progress.toFloat().coerceIn(0.0f, 1.0f)
	}

	private fun getForPlayer(player: Player): DisplayTemplate? {
		if (player.world === WorldManager.getWorld(WorldManager.UHCWorldType.LOBBY)) {
			val pregame = UHC.getPregame()
			val startGameTimer = UHC.startGameTimer.get()

			return if (pregame == null) {
				DisplayTemplate(
					Component.text("UHC Lobby - Game in progress"),
					1.0,
					BossBar.Color.WHITE,
					BossBar.Overlay.PROGRESS,
					null
				)
			} else if (startGameTimer == null) {
				DisplayTemplate(
					Component.text("UHC Lobby - ${pregame.numReadyPlayers()} out of ${pregame.minReadyPlayers} players ready"),
					pregame.numReadyPlayers() / pregame.minReadyPlayers.toDouble(),
					BossBar.Color.WHITE,
					BossBar.Overlay.PROGRESS,
					null
				)
			} else {
				DisplayTemplate(
					Component.text("UHC Lobby - Game starting in ${TickTime.toTimeString(startGameTimer.remaining())}"),
					1.0 - startGameTimer.along(),
					BossBar.Color.WHITE,
					BossBar.Overlay.PROGRESS,
					null
				)
			}
		}

		val game = UHC.getGame() ?: return null

		val phaseAlong = game.getPhaseAlong()
		val phaseType = phaseAlong.phase.type
		val borderRadius = UHCBorder.getCurrentRadius(game)

		val respawnTimerResult = game.playerRespawnTimers.get().find { (timer) ->
			timer.uuid == player.uniqueId
		}

		if (respawnTimerResult != null) {
			return DisplayTemplate(
				Component.text("Respawn in ${TickTime.toTimeString(respawnTimerResult.remaining())}"),
				respawnTimerResult.along(),
				BossBar.Color.WHITE,
				BossBar.Overlay.NOTCHED_12,
				null
			)
		}

		return when (phaseType) {
			PhaseType.GRACE -> DisplayTemplate(
				Component.text(
					"Grace Period - Border shrinks in ${TickTime.toTimeString(phaseAlong.remaining)}",
					phaseType.textColor
				),
				phaseAlong.along,
				phaseType.barColor,
				BossBar.Overlay.NOTCHED_20,
				null
			)

			PhaseType.SHRINK -> DisplayTemplate(
				Component.text(
					"Border radius: $borderRadius - shrinks to ${game.finalRadius} in ${
						TickTime.toTimeString(
							phaseAlong.remaining
						)
					}",
					phaseType.textColor
				),
				phaseAlong.along,
				phaseType.barColor,
				BossBar.Overlay.NOTCHED_20,
				null
			)

			PhaseType.ENDGAME -> {
				val endgame = game.endgamePhase
				val timers = endgame.warningTimers.get()
				val timer = timers.find { (timer) -> timer.uuid == player.uniqueId }

				if (timer == null) return DisplayTemplate(
					Component.text("Endgame", phaseType.textColor),
					1.0,
					phaseType.barColor,
					BossBar.Overlay.NOTCHED_6,
					null
				)

				val showTitle = timer.ticks % 20 == 0

				DisplayTemplate(
					Broadcast.error("Return to y level ${endgame.currentYRange.last} or take damage!"),
					MathUtil.invLerp(0, EndgamePhase.DAMAGE_TIME, timer.ticks),
					BossBar.Color.RED,
					BossBar.Overlay.PROGRESS,
					if (showTitle) Title.title(Component.empty(), Broadcast.error("TOO HIGH!"), 0, 0, 20) else null
				)
			}
		}
	}

	fun tick() {
		Bukkit.getOnlinePlayers().forEach { player ->
			val template = getForPlayer(player)

			val playerBar = playerToBar.get(player)

			if (playerBar == null) {
				if (template != null) {
					val playerBar =
						BossBar.bossBar(template.name, template.floatProgress(), template.color, template.overlay)
					playerToBar[player] = playerBar
				}
			} else {
				if (template == null) {
					player.hideBossBar(playerBar)
				} else {
					playerBar.name(template.name)
					playerBar.progress(template.floatProgress())
					playerBar.color(template.color)
					playerBar.overlay(template.overlay)

					player.showBossBar(playerBar)
				}
				playerBar.name()
			}
		}
	}

	class Events : Listener {
		@EventHandler
		fun onLeave(event: PlayerQuitEvent) {
			playerToBar.remove(event.player)
		}
	}
}