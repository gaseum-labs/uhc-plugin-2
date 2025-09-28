package org.gaseumlabs.uhcplugin.database

import org.bukkit.World
import java.sql.*

object Database {
	private var databaseFile: DatabaseFile? = null

	fun load() {
		databaseFile = DatabaseFile.loadDatabaseFile()
	}

	fun isOpen() = databaseFile != null

	fun getSeed(environment: World.Environment): String? {
		val databaseFile = databaseFile ?: return null

		val connection = DriverManager.getConnection(databaseFile.url + ":" + databaseFile.token)

		return connection.createStatement().use { statement ->
			val gameSeedColumn = if (environment === World.Environment.NORMAL) "seedGame" else "seedNether"

			statement.executeQuery("""
				SELECT s.seed seed
				FROM worldSeed s
				LEFT JOIN game g ON g.${gameSeedColumn} = s.seed
				WHERE s.environment = '${environment.name}' AND g.id IS NULL
				ORDER BY RANDOM()
				LIMIT 1;
			""".trimIndent()).use { resultSet ->
				var seed: String? = null

				while (resultSet.next()) {
					seed = resultSet.getString(1)
				}

				return@use seed
			}
		}
	}
}
