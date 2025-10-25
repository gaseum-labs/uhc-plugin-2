package org.gaseumlabs.uhcplugin.dataStore

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

object DataStore {
	fun getPluginFolder(): File {
		val file = File("./plugins/UHCPlugin2")
		if (!file.exists()) file.mkdirs()
		return file
	}

	inline fun <reified T> write(filename: String, data: T) {
		val folder = getPluginFolder()
		val file = File(folder, filename)
		file.writeText(Json.encodeToString(data))
	}

	inline fun <reified T> read(filename: String, createDefault: () -> T): T {
		val folder = getPluginFolder()
		val file = File(folder, filename)

		if (!file.exists()) {
			val def = createDefault()
			write(filename, def)
			return def
		}

		try {
			return Json.decodeFromString<T>(file.readText())
		} catch (ex: IllegalArgumentException) {
			System.err.println("Invalid format when reading $filename")
			return createDefault()
		}
	}

	class UUIDSerializer : KSerializer<UUID> {
		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("uhcplugin.UUID", PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, value: UUID) {
			encoder.encodeString(value.toString())
		}

		override fun deserialize(decoder: Decoder): UUID {
			return UUID.fromString(decoder.decodeString())
		}
	}
}
