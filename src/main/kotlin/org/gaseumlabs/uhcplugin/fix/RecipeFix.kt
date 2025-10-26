package org.gaseumlabs.uhcplugin.fix

import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey

object RecipeFix {
	val allRecipes = ArrayList<NamespacedKey>()

	fun init() {
		val recipeIterator = Bukkit.recipeIterator()
		val recipes = recipeIterator.asSequence().mapNotNull { recipe ->
			if (recipe is Keyed) recipe.key else null
		}.toList()
		allRecipes.addAll(recipes)
	}
}
