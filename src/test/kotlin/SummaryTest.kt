import org.gaseumlabs.uhcplugin.core.playerData.SummaryPlayerData
import org.gaseumlabs.uhcplugin.core.record.Ledger
import org.gaseumlabs.uhcplugin.core.record.LedgerKill
import org.gaseumlabs.uhcplugin.core.record.Summary
import org.gaseumlabs.uhcplugin.core.team.SummaryTeam
import java.util.*
import kotlin.test.Test

class SummaryTest {
	class TestPlayerData(val uuid: UUID) : SummaryPlayerData {
		var team: TestTeam? = null
		override fun team(): SummaryTeam = team!!
		override fun uuid(): UUID = uuid
	}

	class TestTeam(val uuid: UUID, val name: String) : SummaryTeam {
		var members = ArrayList<TestPlayerData>()
		override fun members(): List<SummaryPlayerData> = members
		override fun name(): String = name
		override fun uuid(): UUID = uuid
	}

	@Test
	fun test() {
		val p0 = UUID.randomUUID()
		val p1 = UUID.randomUUID()
		val p2 = UUID.randomUUID()
		val p3 = UUID.randomUUID()

		val nameMap = mapOf(
			p0 to "Player 0",
			p1 to "Player 1",
			p2 to "Player 2",
			p3 to "Player 3",
		)

		val teams = arrayListOf(
			TestTeam(UUID.randomUUID(), "Team 0"),
			TestTeam(UUID.randomUUID(), "Team 1"),
			TestTeam(UUID.randomUUID(), "Team 2"),
			TestTeam(UUID.randomUUID(), "Team 3"),
		)

		val playerDatas = arrayListOf(
			TestPlayerData(p0),
			TestPlayerData(p1),
			TestPlayerData(p2),
			TestPlayerData(p3),
		)

		playerDatas[0].team = teams[0]
		playerDatas[1].team = teams[1]
		playerDatas[2].team = teams[2]
		playerDatas[3].team = teams[3]

		teams[0].members.add(playerDatas[0])
		teams[1].members.add(playerDatas[1])
		teams[2].members.add(playerDatas[2])
		teams[3].members.add(playerDatas[3])

		val ledger = Ledger()
		ledger.kills.add(LedgerKill(20, p0, p3))
		ledger.kills.add(LedgerKill(21, p1, p3))
		ledger.kills.add(LedgerKill(22, p2, p3))

		val players = Summary.createPlayers(ledger, teams, 123, teams[3], nameMap)

		/*
		 * expected order:
		 * p3: place 1,
		 * p2: place 2,
		 * p1: place 3,
		 * p0: place 4
		 */

		val s = 3
	}
}
