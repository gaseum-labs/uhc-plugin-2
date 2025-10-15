package org.gaseumlabs.uhcplugin.core

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.gaseumlabs.uhcplugin.core.broadcast.MSG
import org.gaseumlabs.uhcplugin.core.game.StartGameMode
import org.gaseumlabs.uhcplugin.core.phase.EndgamePhase
import org.gaseumlabs.uhcplugin.core.phase.PhaseType
import org.gaseumlabs.uhcplugin.core.timer.TickTime
import org.gaseumlabs.uhcplugin.help.LobbyTips
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
		val actionBar: Component?,
	) {
		fun floatProgress() = progress.toFloat().coerceIn(0.0f, 1.0f)
	}

	private fun getForPlayer(player: Player): DisplayTemplate? {
		if (player.world === WorldManager.lobby) {
			val preGame = UHC.preGame()
			val startGameTimer = preGame?.startGameTimer?.get()

			val lobbyTip = LobbyTips.getPlayerTip(player.uniqueId)?.let { MSG.game(it) }

			return if (preGame == null) {
				DisplayTemplate(
					Component.text("UHC Lobby - Game in progress"),
					1.0,
					BossBar.Color.WHITE,
					BossBar.Overlay.PROGRESS,
					null,
					lobbyTip
				)
			} else if (preGame.startGameMode === StartGameMode.HOST) {
				DisplayTemplate(
					Component.text("UHC Lobby - Waiting for host to start game"),
					1.0,
					BossBar.Color.WHITE,
					BossBar.Overlay.PROGRESS,
					null,
					lobbyTip
				)
			} else if (startGameTimer == null) {
				DisplayTemplate(
					Component.text("UHC Lobby - ${preGame.numReadyPlayers()} out of ${preGame.minReadyPlayers} players ready"),
					preGame.numReadyPlayers() / preGame.minReadyPlayers.toDouble(),
					BossBar.Color.WHITE,
					BossBar.Overlay.PROGRESS,
					null,
					lobbyTip
				)
			} else {
				DisplayTemplate(
					Component.text("UHC Lobby - Game starting in ${TickTime.toTimeString(startGameTimer.remaining())}"),
					1.0 - startGameTimer.along(),
					BossBar.Color.WHITE,
					BossBar.Overlay.PROGRESS,
					null,
					lobbyTip
				)
			}
		}

		UHC.activeGame()?.let { activeGame ->
			val phaseAlong = activeGame.getPhaseAlong()
			val phaseType = phaseAlong.phase.type
			val borderRadius = UHCBorder.getBorderRadius(activeGame.gameWorld.worldBorder)

			val respawnTimerResult = activeGame.playerRespawnTimers.get().find { (timer) ->
				timer.uuid == player.uniqueId
			}

			if (respawnTimerResult != null) {
				return DisplayTemplate(
					Component.text("Respawn in ${TickTime.toTimeString(respawnTimerResult.remaining())}"),
					respawnTimerResult.along(),
					BossBar.Color.WHITE,
					BossBar.Overlay.NOTCHED_12,
					null,
					null
				)
			}

			return when (phaseType) {
				PhaseType.GRACE -> DisplayTemplate(
					Component.text(
						"Grace Period - Border shrinks in ${TickTime.toTimeString(phaseAlong.remaining)}",
						phaseType.textColor
					),
					1.0 - phaseAlong.along,
					phaseType.barColor,
					BossBar.Overlay.NOTCHED_20,
					null,
					null
				)

				PhaseType.SHRINK -> DisplayTemplate(
					Component.text(
						"Border radius: $borderRadius - shrinks to ${activeGame.finalRadius} in ${
							TickTime.toTimeString(
								phaseAlong.remaining
							)
						}",
						phaseType.textColor
					),
					1.0 - phaseAlong.along,
					phaseType.barColor,
					BossBar.Overlay.NOTCHED_20,
					null,
					null
				)

				PhaseType.ENDGAME -> {
					val endgame = activeGame.endgamePhase
					val timers = endgame.warningTimers.get()
					val timer = timers.find { (timer) -> timer.uuid == player.uniqueId }
					val verticalRange = endgame.getAlongYRange(phaseAlong.along)

					if (timer == null) return DisplayTemplate(
						Component.text(
							"Endgame - vertical range ${verticalRange.first} - ${verticalRange.last}",
							phaseType.textColor
						),
						1.0 - phaseAlong.along,
						phaseType.barColor,
						BossBar.Overlay.NOTCHED_6,
						null,
						null
					)

					val showTitle = timer.ticks % 20 == 0

					DisplayTemplate(
						MSG.error("Return to y level ${verticalRange.last} or take damage!"),
						MathUtil.invLerp(0, EndgamePhase.DAMAGE_TIME, timer.ticks),
						BossBar.Color.RED,
						BossBar.Overlay.PROGRESS,
						if (showTitle) Title.title(Component.empty(), MSG.error("TOO HIGH!"), 0, 0, 20) else null,
						null
					)
				}
			}
		}

		UHC.postGame()?.let {
			return DisplayTemplate(
				Component.text("Post Game"),
				1.0,
				BossBar.Color.WHITE,
				BossBar.Overlay.PROGRESS,
				null,
				null
			)
		}

		return null
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


			if (template?.title != null) player.showTitle(template.title)
			template?.actionBar?.let { actionBar -> player.sendActionBar(actionBar) }
		}
	}

	class Events : Listener {
		@EventHandler
		fun onLeave(event: PlayerQuitEvent) {
			playerToBar.remove(event.player)
		}
	}
}