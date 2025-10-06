package org.gaseumlabs.uhcplugin.core.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.gaseumlabs.uhcplugin.Reflection
import org.gaseumlabs.uhcplugin.UHCPlugin
import org.gaseumlabs.uhcplugin.core.UHC
import org.gaseumlabs.uhcplugin.core.game.ActiveGame
import kotlin.jvm.optionals.getOrNull

object UHCProtocol {
	lateinit var protocolManager: ProtocolManager

	fun init() {
		protocolManager = ProtocolLibrary.getProtocolManager()

		protocolManager.addPacketListener(
			object : PacketAdapter(
				UHCPlugin.self,
				ListenerPriority.NORMAL,
				PacketType.Play.Server.TRACKED_WAYPOINT
			) {
				override fun onPacketSending(event: PacketEvent) {
					val game = UHC.activeGame() ?: return
					val playerData = game.playerDatas.getActive(event.player) ?: return

					val originalPacket = event.packet.handle as ClientboundTrackedWaypointPacket

					val trackPlayerData = originalPacket.waypoint.id().left().getOrNull()?.let { uuid ->
						game.playerDatas.getActive(uuid)
					}

					val operationOrdinal = Reflection.getEnumOrdinal(originalPacket.operation)

					if ((operationOrdinal == 0 || operationOrdinal == 2) && (trackPlayerData == null || trackPlayerData.team !== playerData.team)) {
						event.isCancelled = true
						return
					}
				}
			}
		)
	}

	fun untrackAllPlayers(player: Player, activeGame: ActiveGame) {
		val connection = (player as CraftPlayer).handle.connection

		val playerData = activeGame.playerDatas.get(player)

		Bukkit.getOnlinePlayers().forEach { otherPlayer ->
			if (player === otherPlayer) return@forEach

			val otherTeam = activeGame.playerDatas.getActive(otherPlayer)?.team

			if (playerData?.team === otherTeam) return@forEach

			connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(otherPlayer.uniqueId))
		}
	}
}

