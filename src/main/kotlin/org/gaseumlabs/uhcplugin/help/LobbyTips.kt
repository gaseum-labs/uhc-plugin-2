package org.gaseumlabs.uhcplugin.help

import org.bukkit.Bukkit
import java.util.*

object LobbyTips {
	private val list = arrayOf(
		"Potions of Strength are disabled",
		"Type /uhc lobby to return to lobby when spectating",
		"Type /uhc forfeit to immediately lose the game",
		"Type /uhc spectate to spectate an ongoing game",
		"Type /uhc ready to participate in a new game",
		"Type /uhc unready if you no longer want to play",
		"Chat messages are automatically sent to your team chat",
		"Type ![your message] to send a message to all chat",
		"PVP is disabled in Grace Period",
		"You are immune to fire in Grace Period",
		"Horse CHC is not real",
		"Grace period lasts 20 minutes",
		"The map extends 384 blocks in each direction",
		"The final border radius is 44 blocks",
		"The border shrinks from 384 to a 44 block radius during Shrink Period",
		"You can regenerate health by eating in Grace Period",
		"You cannot regenerate health with food after Grace Period",
		"Oxeye Daisies can be turned into Suspicious Stews for regeneration",
		"Glistering Melons are used to brew Instant Health potions",
		"There is always a Nether Fortress at the center of the Nether",
		"Collect Nether Wart from chests in the Fortress",
		"There is a y-level range that shrinks in Endgame",
		"Shrink Period lasts 20 minutes",
		"The game is over when only one team survives",
		"In Endgame, a bedrock wall pushes you up from below",
		"Every 20 seconds, glowing is applied for 5 seconds in Endgame",
		"Blocks are destroyed from the top of the world in Endgame",
		"You will take damage if you are too high up in Endgame",
		"When you log out, a zombie will save your position",
		"The Nether closes in Endgame",
		"You may respawn without penalty in Grace Period",
		"Death becomes permanent in Endgame",
		"You may respawn in Shrink Period, but you will lose health",
		"You do not get credit for team kills",
		"Getting kills increases your score",
		"Your placement is determined by long your team survives",
		"Friendly fire is enabled",
		"Keep inventory is disabled",
		"Being outside the border inflicts 1 damage per second",
		"Throwing an Ender Pearl causes no damage",
		"Teams are distributed across the map at game start",
		"Games of less than 6 players are played in teams of 1",
		"Use /link [minecraft username] in Discord to link your Discord account",
		"You will be placed in a voice channel with your teammate in Discord",
		"Nether Portals link to the same coordinates in Overworld and Nether",
		"UHC Advancements show you how to play the game",
		"UHC Advancements show you how to play the game",
		"You can double your Arrows by crafting Spectral Arrows from Glowstone Dust",
		"This plugin is custom-made by Balduvian",
		"If an opponent hides behind their Shield, an Axe swing will disable it for 5 seconds",
		"If an opponent hides behind their Shield, consider enchanting a Crossbow with Piercing",
		"If you want to stay hidden while in inventories, enable Sneak: Toggle in Accessibility Settings",
		"Type /sharecoords to save your location or share it with teammates",
		"To avoid adding Potion ingredients by hand, place a Hopper above your Brewing Stand",
		"Place a door to create an air pocket underwater",
		"Use Lava Buckets for your smelting when deep underground",
	)

	data class PlayerTips(val uuid: UUID, var index: Int, val order: IntArray) {
		fun reset() {
			order.shuffle()
			index = 0
		}
	}

	private val playerTips = ArrayList<PlayerTips>()

	private fun createPlayerTips(uuid: UUID): PlayerTips {
		val order = IntArray(list.size) { i -> i }
		order.shuffle()
		return PlayerTips(uuid, 0, order)
	}

	fun tick(timer: Int) {
		if (timer % 20 != 0) return

		val isIncrement = timer % 200 == 0

		val onlinePlayers = Bukkit.getOnlinePlayers().map { it.uniqueId }

		playerTips.removeIf { tips -> onlinePlayers.none { uuid -> tips.uuid == uuid } }
		playerTips.forEach { tips ->
			if (isIncrement) ++tips.index
			if (tips.index >= list.size) tips.reset()
		}
		playerTips.addAll(
			onlinePlayers
				.filter { uuid -> playerTips.none { tips -> tips.uuid == uuid } }
				.map { uuid -> createPlayerTips(uuid) }
		)
	}

	fun getPlayerTip(uuid: UUID): String? {
		val tips = playerTips.find { tips -> tips.uuid == uuid } ?: return null
		return list[tips.order[tips.index]]
	}
}
