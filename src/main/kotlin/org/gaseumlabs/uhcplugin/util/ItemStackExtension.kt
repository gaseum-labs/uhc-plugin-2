package org.gaseumlabs.uhcplugin.util

import org.bukkit.inventory.ItemStack

fun ItemStack.coerceEmpty(): ItemStack? = if (isEmpty) null else this
