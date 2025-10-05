package org.gaseumlabs.uhcplugin.core.record

import java.util.*

data class LedgerKill(val ticks: Int, val player: UUID, val killer: UUID?)

class Ledger {
	val kills = ArrayList<LedgerKill>()
	var winnerUuid: UUID? = null
}
