package org.gaseumlabs.uhcplugin.fix

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import org.gaseumlabs.uhcplugin.core.broadcast.MSG

class AppleFix : Listener {
    private val countMap: MutableMap<Player, Int> = mutableMapOf()

    @EventHandler
    fun onDropPlayer(event: BlockBreakEvent) {
        val player = event.player
        val block = event.block

        if (isLeaves(block.type) &&
            player.gameMode != GameMode.CREATIVE &&
            !isSilkTouch(player.inventory.itemInMainHand)
        ) {
            val newCount = (countMap[player] ?: 0) + 1
            countMap[player] = newCount

            if (newCount % 200 == 0) {
                block.world.dropItemNaturally(
                    block.location.add(0.5, 0.5, 0.5),
                    ItemType.APPLE.createItemStack(1)
                )
                player.sendMessage(MSG.game("Apple Dropped!"))
            }
        }
    }

    private fun isLeaves(type: Material): Boolean {
        return type.name.endsWith("_LEAVES")
    }

    private fun isSilkTouch(item: ItemStack): Boolean {
        return item.containsEnchantment(Enchantment.SILK_TOUCH) || item.type == Material.SHEARS
    }
}
