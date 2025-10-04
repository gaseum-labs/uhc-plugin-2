package org.gaseumlabs.uhcplugin.world

import org.bukkit.*
import java.io.File
import kotlin.random.Random

object WorldManager {
	private lateinit var lobbyWorld: World

	fun init() {
		lobbyWorld = Bukkit.getWorld("world") ?: throw Exception("World does not exist")
	}

	fun createWorld(uhcWorldType: UHCWorldType, seed: Long?, generateStructures: Boolean): World {
		val world = Bukkit.getServer().createWorld(
			WorldCreator(uhcWorldType.worldName)
				.environment(uhcWorldType.environment)
				.type(WorldType.NORMAL)
				.seed(seed ?: Random.nextLong())
				.generateStructures(generateStructures)
		) ?: throw Exception("could not create world ${uhcWorldType.name}")

		initWorld(world)

		return world
	}

	fun destroyWorld(uhcWorldType: UHCWorldType) {
		val server = Bukkit.getServer()

		server.unloadWorld(uhcWorldType.worldName, false)
		val worldsDirectory = server.worldContainer
		val worldFile = File(worldsDirectory, uhcWorldType.worldName)
		if (worldFile.isDirectory) worldFile.deleteRecursively()
	}

	fun initWorld(world: World) {
		world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
		world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false)
		world.difficulty = Difficulty.NORMAL
	}

	fun getWorld(type: UHCWorldType): World {
		return Bukkit.getWorld(type.worldName) ?: throw Exception("World does not exist")
	}

	val lobby: World
		get() = lobbyWorld
}
