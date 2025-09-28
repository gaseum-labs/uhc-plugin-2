package org.gaseumlabs.uhcplugin.core

import java.util.*

data class LedgerKill(val ticks: Int, val player: UUID, val killer: UUID?)

class Ledger {
	val kills = ArrayList<LedgerKill>()
	var winnerUuid: UUID? = null
}