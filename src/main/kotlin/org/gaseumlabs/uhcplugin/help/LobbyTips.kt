package org.gaseumlabs.uhcplugin.help

import org.bukkit.Bukkit
import java.util.*

object LobbyTips {
	private val list = arrayOf(
		"Potions of strength are banned",
		"Type /uhc lobby to return to lobby when spectating",
		"Type /uhc forfeit to immediately lose the game",
		"Type /uhc spectate to spectate an ongoing game from the lobby",
		"Type /uhc ready to participate in a new game",
		"Type /uhc unready if you no longer want to play",
		"PVP is disabled in grace period",
		"You are immune to fire in grace period",
		"Horse CHC is not real",
		"Grace period lasts 20 minutes",
		"The map size is 512 blocks in every direction",
		"The final border radius is 72 blocks",
		"The border shrinks from 512 to 72 block radius during shrink",
		"You can regenerate health in grace period",
		"You cannot regenerate health after grace period",
		"Oxeye daisy is the flower ingredient in regeneration suspicious stew",
		"Glistering melons are used to brew potions of instant health",
		"There is always a nether fortress in the center of the nether",
		"Nether wart generates randomly in the nether",
		"There is a y level border that shrinks in endgame",
		"Shrink lasts 20 minutes",
		"The game is over when only one team survives",
		"In endgame, a bedrock wall pushed you up from below",
		"Every 20 seconds, glowing is applied for 2 seconds in endgame",
		"Blocks are destroyed from the top of the world in endgame",
		"You will take damage if you are too high in endgame",
		"When you log out, a zombie will stand in for you",
		"The nether closes in endgame",
		"You may respawn without penalty in grace period",
		"Death is permanent in endgame",
		"You may respawn in shrink, but you will lose maximum hearts",
		"Games are played in teams of 2",
		"You do not get credit for team kills",
		"Getting kills increases your score",
		"Your ranking is based on how long your team survives",
		"Friendly fire is enabled",
		"Keep inventory is disabled",
		"You respawn in 15 seconds",
		"Being outside the border at any distance makes you take 1 damage per second",
		"Throwing and ender pearl causes no damage",
		"Teams are distributed across the map to start",
		"Games of less than 6 players are played in teams of 1",
		"Use /link [minecraft username] in discord to link your discord account",
		"You will be placed in a voice channel with your teammate in discord",
		"Nether portals link to the same coordinates in overworld and nether",
		"UHC Advancements show you how to play the game",
		"UHC Advancements show you how to play the game",
		"You can double your Arrows by crafting Spectral Arrows with Glowstone Dust!",
		"This plugin is custom-made by Balduvian",
		"If an opponent hides behind their Shield, an Axe swing will disable it for 5 seconds!",
		"If an opponent hides behind their Shield, consider enchanting a Crossbow with Piercing!",
		"If you want to stay hidden while in inventories, enable Sneak: Toggle in Accessibility Settings!",
		"To save a location or regroup with your teammate, use /sharecoords to show your location in team chat!",
		"To avoid adding Potion ingredients by hand, place a Hopper above your Brewing Stand!"
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

		val onlinePlayers = Bukkit.getOnlinePlayers().map { it.uniqueId }

		playerTips.removeIf { tips -> onlinePlayers.none { uuid -> tips.uuid == uuid } }
		playerTips.forEach { tips ->
			++tips.index
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
