package org.gaseumlabs.uhcplugin.help

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.gaseumlabs.uhcplugin.UHCPlugin
import org.gaseumlabs.uhcplugin.util.JSONWrite

class UHCAdvancement(
	val key: NamespacedKey,
	val parent: UHCAdvancement? = null,
	val icon: ItemStack,
	val title: Component,
	val description: Component,
	val frame: Frame = Frame.TASK,
	val background: String? = null,
	val showToast: Boolean = true,
	val announceToChat: Boolean = true,
	val hidden: Boolean = false,
	val criteria: Criteria,
	val experienceReward: Int? = null,
) {
	val criteriaNames: Set<String>
		get() = criteria.list.keys

	val children = ArrayList<UHCAdvancement>()

	enum class Frame(val serialName: String) {
		TASK("task"),
		GOAL("goal"),
		CHALLENGE("challenge")
	}

	class Criteria() {
		val list = HashMap<String, Trigger>()
		fun add(name: String, trigger: Trigger): Criteria {
			list[name] = trigger
			return this
		}

		companion object {
			fun impossible(): Criteria {
				return Criteria().add(
					"base", Trigger(
						Trigger.Type.IMPOSSIBLE,
						JSONWrite().obj { }
					))
			}

			fun obtainTag(tag: String): Criteria {
				return Criteria().add(
					"obtain",
					Trigger(
						Trigger.Type.INVENTORY_CHANGED,
						JSONWrite().obj {
							it.property("items").array {
								it.obj {
									it.property("items").value(tag)
								}
							}
						}
					)
				)
			}

			fun obtainItemList(vararg materials: Material): Criteria {
				return Criteria().add(
					"obtain",
					Trigger(
						Trigger.Type.INVENTORY_CHANGED,
						JSONWrite().obj {
							it.property("items").array {
								it.obj {
									it.property("items").array { write ->
										materials.forEach {
											write.value(it.key.toString())
										}
									}
								}
							}
						}
					)
				)
			}

			fun obtainAllItemList(vararg materials: Material): Criteria {
				return Criteria().add(
					"obtain",
					Trigger(
						Trigger.Type.INVENTORY_CHANGED,
						JSONWrite().obj {
							it.property("items").array { write ->
								materials.forEach { material ->
									write.obj { write ->
										write.property("items").array { write ->
											write.value(material.key.toString())
										}
									}
								}
							}
						}
					)
				)
			}

			fun obtainItem(itemStack: ItemStack): Criteria {
				return Criteria().add(
					"obtain",
					Trigger(
						Trigger.Type.INVENTORY_CHANGED,
						JSONWrite().obj {
							it.property("items").array {
								it.json(UHCPlugin.unsafe.serializeItemAsJson(itemStack).toString())
							}
						}
					)
				)
			}

			fun craftItem(material: Material): Criteria {
				return Criteria().add(
					"craft",
					Trigger(
						Trigger.Type.RECIPE_CRAFTED,
						JSONWrite().obj {
							it.property("recipe_id").value(material.key.toString())
						}
					)
				)
			}

			fun changedDimension(from: NamespacedKey, to: NamespacedKey): Criteria {
				return Criteria().add(
					"move",
					Trigger(
						Trigger.Type.CHANGED_DIMENSION,
						JSONWrite().obj {
							it.property("from").value(from.toString())
							it.property("to").value(to.toString())
						}
					)
				)
			}
		}
	}

	data class Trigger(val type: Type, val conditions: JSONWrite) {
		enum class Type(val serialName: String) {
			ANY_BLOCK_USE("any_block_use"),
			BREWED_POTION("brewed_potion"),
			CHANGED_DIMENSION("changed_dimension"),
			CHANNELED_LIGHTNING("channeled_lightning"),
			CONSUME_ITEM("consume_item"),
			DEFAULT_BLOCK_USE("default_block_use"),
			EFFECTS_CHANGED("effects_changed"),
			ENCHANTED_ITEM("enchanted_item"),
			ENTER_BLOCK("enter_block"),
			ENTITY_HURT_PLAYER("entity_hurt_player"),
			ENTITY_KILLED_PLAYER("entity_killed_player"),
			FALL_FROM_HEIGHT("fall_from_height"),
			LOCATION("location"),
			PLAYER_HURT_ENTITY("player_hurt_entity"),
			PLAYER_KILLED_ENTITY("player_killed_entity"),
			USED_ENDER_EYE("used_ender_eye"),
			IMPOSSIBLE("impossible"),
			INVENTORY_CHANGED("inventory_changed"),
			RECIPE_CRAFTED("recipe_crafted")
		}
	}

	val componentGson = GsonComponentSerializer.gson()

	fun serialize(): String {
		return JSONWrite().obj {
			if (parent != null) it.property("parent").value(parent.key.toString())
			it.property("display").obj {
				it.property("icon").json(Bukkit.getUnsafe().serializeItemAsJson(icon).toString())
				it.property("title").json(componentGson.serialize(title))
				it.property("description").json(componentGson.serialize(description))
				it.property("frame").value(frame.serialName)
				if (background != null) it.property("background").value(background)
				it.property("show_toast").value(showToast)
				it.property("announce_to_chat").value(announceToChat)
				it.property("hidden").value(hidden)
			}
			it.property("criteria").obj { write ->
				criteria.list.forEach { (key, trigger) ->
					write.property(key).obj { write ->
						write.property("trigger").value(trigger.type.serialName)
						write.property("conditions").json(trigger.conditions.toString())
					}
				}
			}
			if (experienceReward != null) {
				it.property("rewards").obj {
					it.property("experience").value(experienceReward)
				}
			}
		}.toString()
	}

	fun append(
		key: NamespacedKey,
		icon: ItemStack,
		title: Component,
		description: Component,
		frame: Frame = Frame.TASK,
		showToast: Boolean = true,
		announceToChat: Boolean = true,
		hidden: Boolean = false,
		criteria: Criteria,
		experienceReward: Int? = null,
	): UHCAdvancement {
		val advancement = UHCAdvancement(
			key = key,
			parent = this,
			icon = icon,
			title = title,
			description = description,
			frame = frame,
			showToast = showToast,
			announceToChat = announceToChat,
			hidden = hidden,
			criteria = criteria,
			experienceReward = experienceReward
		)
		this.children.add(advancement)
		return advancement
	}

	companion object {

		fun createRoot(
			key: NamespacedKey,
			icon: ItemStack,
			title: Component,
			description: Component,
			background: String,
			showToast: Boolean = true,
			announceToChat: Boolean = false,
			hidden: Boolean = false,
			criteria: Criteria,
		): UHCAdvancement {
			val advancement = UHCAdvancement(
				key = key,
				icon = icon,
				title = title,
				description = description,
				background = background,
				showToast = showToast,
				announceToChat = announceToChat,
				hidden = hidden,
				criteria = criteria,
			)
			return advancement
		}
	}
}
