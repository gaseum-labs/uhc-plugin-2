package org.gaseumlabs.uhcplugin.core

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.entity.Player
import org.gaseumlabs.uhcplugin.UHCPlugin
import org.gaseumlabs.uhcplugin.core.phase.EndgamePhase
import org.gaseumlabs.uhcplugin.core.phase.Grace
import org.gaseumlabs.uhcplugin.core.phase.Shrink
import org.gaseumlabs.uhcplugin.core.playerData.OfflineZombie
import org.gaseumlabs.uhcplugin.core.playerData.PlayerCapture
import org.gaseumlabs.uhcplugin.core.playerData.PlayerData
import org.gaseumlabs.uhcplugin.core.playerData.PlayerDatas
import org.gaseumlabs.uhcplugin.core.timer.*
import org.gaseumlabs.uhcplugin.core.timer.Timer
import org.gaseumlabs.uhcplugin.fix.BorderFix
import org.gaseumlabs.uhcplugin.world.Seed
import org.gaseumlabs.uhcplugin.world.WorldManager
import java.util.*

object UHC {
	private var game: Game? = null
	private var pregame: Pregame = Pregame.create()

	val startGameTimer = SingleTimerHolder<Timer>()

	val RESPAWN_TIME = TickTime.ofSeconds(15)

	fun init() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.self, UHC::tick, 0, 1)
	}

	fun tick() {
		val game = game

		if (game != null && !game.isDone) gameTick(game)
		if (game == null) lobbyTick(pregame)

		Display.tick()

		game?.postTick()
		startGameTimer.postTick()
		game?.endgamePhase?.warningTimers?.postTick()
	}

	fun lobbyTick(pregame: Pregame) {
		val numReadyPlayers = pregame.numReadyPlayers()

		val result = startGameTimer.get()
		if (result == null) {
			if (numReadyPlayers >= pregame.minReadyPlayers) {
				startGameTimer.set(CountdownTimer(TickTime.ofSeconds(10)))
				Broadcast.broadcast(Broadcast.game("Game starting in 2 minutes"))
			}
		} else {
			if (result.isDone()) {
				startGame(pregame)
			} else if (numReadyPlayers < pregame.minReadyPlayers) {
				startGameTimer.cancel()
				Broadcast.broadcast(Broadcast.game("Game cancelled because not enough players are ready"))
			}
		}
	}

	fun destroyGame() {
		val game = game ?: return;

		moveAllToLobby(game)

		WorldManager.destroyWorld(WorldManager.UHCWorldType.GAME)
		WorldManager.destroyWorld(WorldManager.UHCWorldType.NETHER)

		this.game = null
		this.pregame = Pregame.create()
	}

	fun moveToLobby(player: Player) {
		val lobbyWorld = WorldManager.getWorld(WorldManager.UHCWorldType.LOBBY)
		PlayerManip.resetPlayer(player, Lobby.GAME_MODE, 20.0, lobbyWorld.spawnLocation)
	}

	fun moveAllToLobby(game: Game) {
		val lobbyWorld = WorldManager.getWorld(WorldManager.UHCWorldType.LOBBY)
		Bukkit.getOnlinePlayers().forEach { player ->
			if (player.world === game.gameWorld || player.world === game.netherWorld) {
				PlayerManip.resetPlayer(player, Lobby.GAME_MODE, 20.0, lobbyWorld.spawnLocation)
			}
		}
	}

	fun startGame(pregame: Pregame) {
		destroyGame()

		val gameWorld = WorldManager.createWorld(WorldManager.UHCWorldType.GAME, Seed.goodSeedList.random(), true)
		val netherWorld = WorldManager.createWorld(WorldManager.UHCWorldType.NETHER, null, true)

		val borderRadius = pregame.getInitialRadius()
		val finalRadius = pregame.getEndgameRadius()

		val finalYRange = EndgamePhase.getFinalYRange(gameWorld, finalRadius, pregame.gameConfig.finalYLevels)

		UHCBorder.set(gameWorld, borderRadius)
		UHCBorder.set(netherWorld, borderRadius)

		gameWorld.time = 0L
		netherWorld.time = 0L

		gameWorld.setStorm(false)
		gameWorld.weatherDuration = 0
		gameWorld.clearWeatherDuration = 9999999

		gameWorld.pvp = false

		pregame.playerUUIDToLocation = PlayerSpreader.getPlayerLocations(
			gameWorld,
			PlayerSpreader.CONFIG_DEFAULT,
			borderRadius,
			pregame.readyPlayers.map { uuid -> listOf(uuid) }
		)

		val playerDatas = PlayerDatas.create(pregame.playerUUIDToLocation.map { (uuid, location) ->
			PlayerData.create(uuid)
		})

		val game = Game(
			playerDatas = playerDatas,
			gracePhase = Grace(pregame.gameConfig.graceDuration),
			shrinkPhase = Shrink(pregame.gameConfig.shrinkDuration),
			endgamePhase = EndgamePhase(finalYRange),
			borderRadius,
			finalRadius,
			gameWorld,
			netherWorld
		)

		game.playerDatas.active.forEach { playerData ->
			val location = pregame.playerUUIDToLocation.get(playerData.uuid)!!

			playerData.executeAction { player ->
				PlayerManip.resetPlayer(
					player,
					GameMode.SURVIVAL,
					playerData.getMaxHealth(),
					location
				)
			}.onNoZombie {
				playerData.offlineRecord.onSpawn(
					OfflineZombie.spawn(
						playerData.uuid,
						PlayerCapture.createInitial(location, playerData.getMaxHealth())
					)
				)
			}
		}

		this.pregame = Pregame.create()
		this.game = game

		Bukkit.getOnlinePlayers().forEach { player ->
			player.updateCommands()
		}
	}

	fun gameTick(game: Game) {
		game.playerRespawnTimers.get().forEach { result ->
			if (result.isDone()) {
				val playerData = game.playerDatas.get(result.timer.uuid) ?: return@forEach

				val respawnLocation = PlayerSpreader.getSingleLocation(
					playerData.uuid,
					game.gameWorld,
					UHCBorder.getCurrentRadius(game),
					PlayerSpreader.CONFIG_DEFAULT
				)

				if (respawnLocation == null) {
					game.playerRespawnTimers.add(RespawnTimer(playerData.uuid, 1))
				} else {
					playerData.executeAction { player ->
						PlayerManip.resetPlayer(
							player,
							GameMode.SURVIVAL,
							playerData.getMaxHealth(),
							respawnLocation
						)
					}.onNoZombie {
						playerData.offlineRecord.onSpawn(
							OfflineZombie.spawn(
								playerData.uuid,
								PlayerCapture.createInitial(
									respawnLocation,
									playerData.getMaxHealth()
								)
							)
						)
					}
				}
			}
		}

		if (game.isShrinkStarting()) {
			game.shrinkPhase.start(game)
		}
		if (game.isEndgameStarting()) {
			game.endgamePhase.start(game)
		}

		val phaseAlong = game.getPhaseAlong()
		val phase = phaseAlong.phase
		if (phase is EndgamePhase) {
			game.endgamePhase.tick(game, phaseAlong)
		}

		BorderFix.tick(game)
	}

	fun endGame(game: Game) {
		game.isDone = true
		UHCBorder.stop(game.gameWorld)
		game.gameWorld.pvp = false
		game.gameWorld.setGameRule(GameRule.NATURAL_REGENERATION, false)
		game.gameWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
		game.gameWorld.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
		game.gameWorld.setGameRule(GameRule.DO_FIRE_TICK, false)
	}

	fun getGame(): Game? = game
	fun getPregame(): Pregame? = if (game == null) pregame else null
	fun isPregame(): Boolean = getPregame() != null
	fun isGame(): Boolean = game != null

	fun onPlayerDeath(
		game: Game,
		playerData: PlayerData,
		deathLocation: Location,
		killerUuid: UUID?,
		deathMessage: Component,
		forcePermanent: Boolean,
	) {
		val phase = game.getPhase().type

		if (phase.deathCounts || forcePermanent) playerData.numDeaths += 1

		playerData.offlineRecord.onDeath(deathLocation)

		playerData.executeAction { player ->
			PlayerManip.makeSpectator(player, null)
		}

		val isElimination = if (!forcePermanent && playerData.canRespawn() && phase.canRespawn) {
			game.playerRespawnTimers.add(RespawnTimer(playerData.uuid, RESPAWN_TIME))
			false
		} else {
			playerData.isActive = false
			true
		}

		game.ledger.kills.add(LedgerKill(game.timer, playerData.uuid, killerUuid))

		val remainingPlayers = game.playerDatas.active.size

		Broadcast.broadcastGame(game, deathMessage)

		val deathPlayer = Bukkit.getOfflinePlayer(playerData.uuid)

		if (remainingPlayers == 0) {
			Broadcast.broadcastGame(game, Broadcast.game("No one wins?"))
			endGame(game)
		} else if (remainingPlayers == 1) {
			val winner = Bukkit.getOfflinePlayer(game.playerDatas.active[0].uuid)
			Broadcast.broadcastGame(game, Broadcast.game("${winner.name ?: "An unknown player"} wins!"))
			endGame(game)
		} else {
			if (isElimination) Broadcast.broadcastGame(
				game,
				Broadcast.game("${deathPlayer.name ?: "An unknown player"} eliminated, $remainingPlayers players remain")
			)
		}
	}
}