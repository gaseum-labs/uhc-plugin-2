package org.gaseumlabs.uhcplugin.database

import java.io.File

data class  DatabaseFile(val url: String, val token: String) {
	companion object {
		val file = File("uhcdb.env")

		fun loadDatabaseFile(): DatabaseFile? {
			if (!file.isFile) {
				System.err.println("no uhcdb.env file found, creating one\nyou will not be able to use the database until you fill it out")
				file.writeText("#fill in the correct database credentials here\nDATABASE_URL=\nDATABASE_TOKEN=")
				return null
			}

			var url: String? = null
			var token: String? = null

			val lines = file.readLines()
			for (line in lines) {
				val splitIndex = line.indexOf('=')
				val fieldName = line.substring(0..<splitIndex)
				val value = line.substring(splitIndex + 1)

				if (value.isEmpty()) continue

				if (fieldName == "DATABASE_URL") {
					url = value
				} else if (fieldName == "DATABASE_TOKEN") {
					token = value
				}
			}

			if (url == null || token == null) {
				System.err.println("no value found for either DATABASE_URL or DATABASE_TOKEN")
				return null
			}

			return DatabaseFile(url, token)
		}
	}
}
