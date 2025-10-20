package org.gaseumlabs.uhcplugin.discord

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.gaseumlabs.uhcplugin.core.record.Summary
import org.gaseumlabs.uhcplugin.core.timer.TickTime
import java.time.format.DateTimeFormatter

object SummaryMessage {
	val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
	fun create(summary: Summary): MessageEmbed {

		val builder = EmbedBuilder()
			.setTitle("UHC Summary ${formatter.format(summary.startDate)}")
			.setDescription("Match time: ${TickTime.toTimeString(summary.ticks)}")
			.setColor(0xf0c013)

		summary.players.forEach { player ->
			builder.addField(
				"${player.place}. _[${player.teamName}]_ **${player.name}**${if (player.win) " 👑" else ""}${if (!player.alive) " ☠️" else ""}",
				listOfNotNull(
					if (player.win) "Winner" else null,
					if (player.numKills == 1) "1 kill" else null,
					if (player.numKills > 1) "${player.numKills} kills" else null,
					if (!player.alive && player.killedBy != null) "Killed by ${player.killedBy}" else null,
					if (!player.alive && player.killedBy == null) "Killed by environment" else null
				).joinToString("\n"),
				false
			)

		}

		return builder.build()
	}
}
