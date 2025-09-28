package org.gaseumlabs.uhcplugin.core.team

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

object TeamDialog {
	fun show(player: Player) {
		val dialog = Dialog.create { builder ->
			builder.empty()
				.base(
					DialogBase
						.builder(Component.text("Team Settings"))
						.canCloseWithEscape(true)
						.externalTitle(Component.text("Team Settings"))
						.body(
							listOf(
								DialogBody.plainMessage(Component.text("How your team will appear in the game")),
								DialogBody.plainMessage(Component.text("Team Color")),
								//TODO team colors
							)
						)
						.inputs(
							listOf(
								DialogInput.text("name", Component.text("Team Name")).maxLength(100).build(),
							)
						)
						.build()
				)
				.type(
					DialogType.notice(
						ActionButton.create(
							Component.text("Save"),
							null,
							50,
							null
						)
					)
				)
		}
		player.showDialog(dialog)
	}
}