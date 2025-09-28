package org.gaseumlabs.uhcplugin.discord

import com.google.gson.Gson
import java.io.File
import java.util.*

data class Config(
	val token: String,
	val guildId: Long,
	var summaryChannelId: Long?,
	var voiceChannelId: Long?,
	val playerToUser: HashMap<UUID, Long>,
) {
	fun getLinkByUser(id: Long): UUID? {
		return playerToUser.entries.find { entry -> entry.value == id }?.key
	}

	fun makeLink(uuid: UUID, id: Long) {
		getLinkByUser(id)?.let { uuid -> playerToUser.remove(uuid) }
		playerToUser[uuid] = id
	}

	fun unlink(id: Long): Boolean {
		return getLinkByUser(id)?.let { uuid -> playerToUser.remove(uuid); true } ?: false
	}

	companion object {
		val gson = Gson()

		const val FILENAME = "discord_config.txt"

		fun read(): Config? {
			val file = File(FILENAME)

			if (!file.exists()) {
				file.writeText(
					gson.toJson(
						Config(
							"",
							-1L,
							null,
							null,
							HashMap()
						)
					)
				)
				return null
			}

			return try {
				gson.fromJson(file.reader(), Config::class.java)
			} catch (ex: Exception) {
				null
			}
		}

		fun write(config: Config) {
			val file = File(FILENAME)
			file.writeText(gson.toJson(config))
		}
	}
}
