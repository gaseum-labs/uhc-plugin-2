package org.gaseumlabs.uhcplugin.core

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.Location
import org.gaseumlabs.uhcplugin.UHCPlugin
import org.gaseumlabs.uhcplugin.core.broadcast.Broadcast
import org.gaseumlabs.uhcplugin.core.broadcast.MSG
import org.gaseumlabs.uhcplugin.core.command.CommandUtil
import org.gaseumlabs.uhcplugin.core.game.*
import org.gaseumlabs.uhcplugin.core.phase.EndgamePhase
import org.gaseumlabs.uhcplugin.core.phase.Grace
import org.gaseumlabs.uhcplugin.core.phase.Shrink
import org.gaseumlabs.uhcplugin.core.phase.VerticalBorder
import org.gaseumlabs.uhcplugin.core.playerData.OfflineZombie
import org.gaseumlabs.uhcplugin.core.playerData.PlayerCapture
import org.gaseumlabs.uhcplugin.core.playerData.PlayerData
import org.gaseumlabs.uhcplugin.core.playerData.PlayerDatas
import org.gaseumlabs.uhcplugin.core.protocol.UHCProtocol
import org.gaseumlabs.uhcplugin.core.record.LedgerKill
import org.gaseumlabs.uhcplugin.core.record.Summary
import org.gaseumlabs.uhcplugin.core.team.ActiveTeams
import org.gaseumlabs.uhcplugin.core.team.Teams
import org.gaseumlabs.uhcplugin.core.team.UHCTeam
import org.gaseumlabs.uhcplugin.core.timer.CountdownTimer
import org.gaseumlabs.uhcplugin.core.timer.TickTime
import org.gaseumlabs.uhcplugin.discord.GameRunnerBot
import org.gaseumlabs.uhcplugin.fix.BorderFix
import org.gaseumlabs.uhcplugin.fix.RecipeFix
import org.gaseumlabs.uhcplugin.help.ChatHelp
import org.gaseumlabs.uhcplugin.help.LobbyTips
import org.gaseumlabs.uhcplugin.help.PlayerAdvancement
import org.gaseumlabs.uhcplugin.help.UHCAdvancements
import org.gaseumlabs.uhcplugin.world.Seed
import org.gaseumlabs.uhcplugin.world.UHCWorldType
import org.gaseumlabs.uhcplugin.world.WorldManager
import java.util.*

object UHC {
	val gameConfig = GameConfig.read()
	private var stage: Stage = PreGame.create()

	val RESPAWN_TIME = 1
	var timer = 0

	fun init() {
		WorldManager.init()
		Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.self, UHC::tick, 0, 1)
		HealthObjective.init()
		Teams.cleanup()
		RecipeFix.init()
	}

	fun tick() {
		when (val game = stage) {
			is PreGame -> lobbyTick(game)
			is ActiveGame -> activeGameTick(game)
			is PostGame -> {}
		}

		LobbyTips.tick(timer)
		Display.tick()
		stage.postTick()
		++timer
		if (timer == 2162160) timer = 0
	}

	fun lobbyTick(preGame: PreGame) {
		val numReadyPlayers = preGame.numReadyPlayers()

		val result = preGame.startGameTimer.get()
		if (result == null) {
			if (numReadyPlayers >= gameConfig.minReadyPlayers && preGame.startGameMode === StartGameMode.READY) {
				preGame.startGameTimer.set(CountdownTimer(TickTime.ofSeconds(10)))
				Broadcast.broadcast(MSG.game("Game starting in 2 minutes"))
			}
		} else {
			if (preGame.startGameMode !== StartGameMode.READY) {
				preGame.startGameTimer.cancel()
			} else if (numReadyPlayers < gameConfig.minReadyPlayers) {
				preGame.startGameTimer.cancel()
				Broadcast.broadcast(MSG.game("Game cancelled because not enough players are ready"))
			} else if (result.isDone()) {
				preGameToActiveGame(preGame)
			}
		}
	}

	fun gameToPreGame(game: Game) {
		moveAllToLobby(game)
		game.destroy()

		this.stage = PreGame.create()

		CommandUtil.updatePlayers()

		GameRunnerBot.instance?.destroyVoiceChannels()
	}

	fun moveAllToLobby(game: Game) {
		Bukkit.getOnlinePlayers().forEach { player ->
			if (player.world === game.gameWorld || player.world === game.netherWorld) {
				PlayerManip.resetPlayer(player, GameMode.ADVENTURE, 20.0, WorldManager.lobby.spawnLocation)
			}
		}
	}

	fun preGameToActiveGame(preGame: PreGame) {
		WorldManager.destroyWorld(UHCWorldType.GAME)
		WorldManager.destroyWorld(UHCWorldType.NETHER)

		val activeTeams = if (preGame.startGameMode === StartGameMode.READY) {
			val teamSize = getAutoTeamSize(preGame)

			val activeTeams = ActiveTeams(emptyList())
			activeTeams.fillRandomTeams(preGame.readyPlayers.toList(), teamSize)

			activeTeams
		} else {
			if (preGame.teams.teams.isEmpty()) throw Exception("No teams to start with")
			ActiveTeams(preGame.teams.teams.map { preTeam -> UHCTeam.fromPreTeam(preTeam) })
		}

		val gameWorld = WorldManager.createWorld(UHCWorldType.GAME, Seed.goodSeedList.random(), true)
		val netherWorld = WorldManager.createWorld(UHCWorldType.NETHER, null, true)

		val borderRadius = gameConfig.initialRadius
		val finalRadius = gameConfig.endgameRadius

		val finalYRange = VerticalBorder.getFinalYRange(gameWorld, finalRadius, gameConfig.finalYLevels)

		val isRanked = activeTeams.teams.sumOf { team -> team.memberUUIDs.size } > 6

		UHCBorder.set(gameWorld, borderRadius)
		UHCBorder.set(netherWorld, borderRadius)

		gameWorld.time = 0L
		netherWorld.time = 0L

		gameWorld.setStorm(false)
		gameWorld.weatherDuration = 0
		gameWorld.clearWeatherDuration = 9999999

		gameWorld.pvp = false

		preGame.playerUUIDToLocation = PlayerSpreader.getPlayerLocations(
			gameWorld,
			PlayerSpreader.CONFIG_DEFAULT,
			borderRadius,
			activeTeams.teams.map { team -> team.memberUUIDs.toList() }
		)

		val playerDatas = PlayerDatas.create(activeTeams.teams.flatMap { team ->
			team.memberUUIDs.map { uuid ->
				PlayerData.createInitial(
					uuid,
					team
				)
			}
		})

		PlayerAdvancement.wipe(Bukkit.getOnlinePlayers())

		val activeGame = ActiveGame(
			playerDatas = playerDatas,
			gracePhase = Grace(gameConfig.graceDuration),
			shrinkPhase = Shrink(gameConfig.shrinkDuration),
			endgamePhase = EndgamePhase(gameConfig.collapseTime, finalYRange),
			borderRadius,
			finalRadius,
			gameWorld,
			netherWorld,
			activeTeams,
			isRanked
		)

		this.stage = activeGame

		activeGame.playerDatas.active.forEach { playerData ->
			val location = preGame.playerUUIDToLocation.get(playerData.uuid)!!

			playerData.executeAction { player ->
				PlayerManip.resetPlayer(
					player,
					GameMode.SURVIVAL,
					playerData.respawnHealth,
					location
				)
				PlayerAdvancement.grant(player, UHCAdvancements.UHC)
			}.onNoZombie {
				playerData.offlineRecord.onGameSpawn(
					OfflineZombie.spawn(
						playerData.uuid,
						PlayerCapture.createInitial(location, playerData.respawnHealth)
					)
				)
			}
		}

		CommandUtil.updatePlayers()

		activeGame.playerDatas.active.forEach { playerData ->
			playerData.executeAction { player ->
				UHCProtocol.untrackAllPlayers(player, activeGame)
			}
		}

		GameRunnerBot.instance?.createVoiceChannels(activeTeams.teams.associate { team ->
			team.uuid to team.memberUUIDs
		})
	}

	fun getAutoTeamSize(preGame: PreGame): Int {
		return if (preGame.numReadyPlayers() >= 6) 2 else 1
	}

	fun activeGameTick(activeGame: ActiveGame) {
		if (activeGame.isShrinkStarting()) {
			activeGame.shrinkPhase.start(activeGame)
		}
		if (activeGame.isEndgameStarting()) {
			activeGame.endgamePhase.start(activeGame)
		}

		val phaseAlong = activeGame.getPhaseAlong()
		val phase = phaseAlong.phase
		if (phase is EndgamePhase) {
			activeGame.endgamePhase.tick(activeGame, phaseAlong)
		}

		activeGame.lootRegen.tick(timer, activeGame)

		BorderFix.tick(activeGame)

		ChatHelp.tick(activeGame)
	}

	fun activeGameToPostGame(activeGame: ActiveGame, winningTeam: UHCTeam?) {
		UHCBorder.stop(activeGame.gameWorld)
		activeGame.gameWorld.pvp = false
		activeGame.gameWorld.setGameRule(GameRule.NATURAL_REGENERATION, false)
		activeGame.gameWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
		activeGame.gameWorld.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
		activeGame.gameWorld.setGameRule(GameRule.DO_FIRE_TICK, false)

		Bukkit.getOnlinePlayers().forEach { player ->
			player.updateCommands()
		}

		val summary = Summary.create(
			activeGame,
			winningTeam,
			Summary.createUUIDToName()
		)

		GameRunnerBot.instance?.sendSummaryMessage(summary)

		activeGame.teams.clearTeams()

		this.stage = PostGame(summary, activeGame.gameWorld, activeGame.netherWorld)

		CommandUtil.updatePlayers()
	}

	fun preGame(): PreGame? = stage as? PreGame
	fun activeGame(): ActiveGame? = stage as? ActiveGame
	fun postGame(): PostGame? = stage as? PostGame
	fun stage(): Stage = stage
	fun game(): Game? = stage as? Game

	fun onPlayerDeath(
		activeGame: ActiveGame,
		playerData: PlayerData,
		deathLocation: Location,
		killerUuid: UUID?,
		deathMessage: Component,
		forcePermanent: Boolean,
	) {
		val phase = activeGame.getPhase()

		if (phase.type.deathCounts || forcePermanent) {
			playerData.numDeaths += 1
			if (phase is Shrink) {
				playerData.respawnHealth = PlayerData.getRespawnHealth(activeGame.getPhaseAlong().along)
			}
		}

		playerData.offlineRecord.onDeath(deathLocation)

		val isElimination = if (!forcePermanent && playerData.canRespawn() && phase.type.canRespawn) {
			false
		} else {
			playerData.isActive = false
			true
		}

		//Broadcast.broadcastGame(activeGame, deathMessage)

		if (!isElimination) return

		val isTeamKill = killerUuid?.let { activeGame.playerDatas.get(it)?.team } === playerData.team
		activeGame.ledger.kills.add(LedgerKill(activeGame.timer, playerData.uuid, if (isTeamKill) null else killerUuid))

		onPlayerEliminate(activeGame, playerData)
	}

	private fun onPlayerEliminate(
		activeGame: ActiveGame,
		playerData: PlayerData,
	) {
		playerData.isActive = false

		val playerTeam = playerData.team

		val activeTeams = getActiveTeams(activeGame)

		val isTeamElimination = activeTeams.none { team -> team === playerTeam }

		if (isTeamElimination) {
			when (activeTeams.size) {
				0 -> {
					Broadcast.broadcastGame(activeGame, MSG.game("No one wins?"))
					activeGameToPostGame(activeGame, null)
				}

				1 -> {
					val winningTeam = activeTeams.first()

					Broadcast.broadcastGame(
						activeGame,
						MSG.game("Team ").append(winningTeam.nameComponent()).append(MSG.game(" wins!"))
					)
					activeGameToPostGame(activeGame, winningTeam)
				}

				else -> {
					Broadcast.broadcastGame(
						activeGame,
						MSG.game("Team ").append(playerTeam.nameComponent())
							.append(MSG.game(" has been eliminated!")),
						MSG.gameBold("${activeTeams.size}").append(MSG.game(" teams remain"))
					)
				}
			}
		}
	}

	private fun getActiveTeams(activeGame: ActiveGame): List<UHCTeam> {
		return activeGame.teams.teams.filter { team -> team.members.any { it.isActive } }
	}
}