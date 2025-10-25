package org.gaseumlabs.uhcplugin.discord

import kotlinx.serialization.Serializable
import org.gaseumlabs.uhcplugin.dataStore.DataStore
import java.util.*

@Serializable
data class Config(
	val token: String,
	val guildId: Long,
	var summaryChannelId: Long?,
	var voiceChannelId: Long?,
	val playerToUser: HashMap<@Serializable(DataStore.UUIDSerializer::class) UUID, Long>,
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
		const val FILENAME = "discord-config.json"

		fun read(): Config = DataStore.read(FILENAME, ::createDefault)

		fun write(config: Config) = DataStore.write(FILENAME, config)

		fun createDefault() = Config(
			"",
			-1L,
			null,
			null,
			HashMap()
		)
	}
}
