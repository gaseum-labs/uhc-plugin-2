package org.gaseumlabs.uhcplugin.core.playerData

import org.gaseumlabs.uhcplugin.core.team.SummaryTeam
import java.util.*

interface SummaryPlayerData {
	fun team(): SummaryTeam
	fun uuid(): UUID
}
