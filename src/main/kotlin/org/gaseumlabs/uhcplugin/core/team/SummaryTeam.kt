package org.gaseumlabs.uhcplugin.core.team

import org.gaseumlabs.uhcplugin.core.playerData.SummaryPlayerData
import java.util.*

interface SummaryTeam {
	fun uuid(): UUID
	fun name(): String
	fun members(): List<SummaryPlayerData>
}