package org.gaseumlabs.uhcplugin.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import org.bukkit.Bukkit
import org.gaseumlabs.uhcplugin.core.record.Summary
import java.util.*
import java.util.concurrent.CompletableFuture

class GameRunnerBot(private val api: JDA, private val config: Config) : ListenerAdapter() {
	fun init(): CompletableFuture<Void> {
		destroyVoiceChannels()

		return uhcGuild.updateCommands().addCommands(
			Commands.slash("setsummarychannel", "Sets the summary channel")
				.addOption(OptionType.CHANNEL, "channel", "The summary channel", true)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER)),
			Commands.slash("setvoicechannel", "Sets the voice channel")
				.addOption(OptionType.CHANNEL, "channel", "The voice channel", true)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER)),
			Commands.slash("link", "Link your discord account to your minecraft account")
				.addOption(OptionType.STRING, "username", "Your minecraft username", true, true),
			Commands.slash("unlink", "Unlink your discord account from your minecraft account"),
			Commands.slash("forcelink", "Create a link (admin)")
				.addOption(OptionType.USER, "user", "Discord user", true)
				.addOption(OptionType.STRING, "username", "minecraft username", true, true)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER)),
			Commands.slash("forceunlink", "Delete a link (admin)")
				.addOption(OptionType.USER, "user", "Discord user", true)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER)),
		).submit().thenRun {
			api.addEventListener(this)
		}
	}

	val teamUuidToNumber = HashMap<UUID, Int>()

	private fun getOrCreateChannel(category: Category, name: String): CompletableFuture<VoiceChannel> {
		val teamChannel = category.voiceChannels.find { channel -> channel.name == name }
		return if (teamChannel != null) {
			CompletableFuture.completedFuture(teamChannel)
		} else {
			category.createVoiceChannel(name).submit()
		}
	}

	fun createVoiceChannels(groups: Map<UUID, Set<UUID>>) {
		try {
			val guild = uhcGuild
			val mainChannel = getVoiceChannel(guild)
			val category = mainChannel.parentCategory ?: throw Exception("voice channel is not in a category")

			teamUuidToNumber.clear()

			groups.entries.forEach { (teamUuid, users) ->
				val channelName = getChannelName(teamUuid)

				getOrCreateChannel(category, channelName).thenAccept { teamChannel ->
					users.forEach { playerUuid ->
						val userId = config.playerToUser[playerUuid] ?: return@forEach
						guild.moveVoiceMember(UserSnowflake.fromId(userId), teamChannel).queue()
					}
				}
			}
		} catch (ex: Exception) {
			ex.printStackTrace()
		}
	}

	fun destroyVoiceChannels() {
		try {
			val guild = uhcGuild
			val mainChannel = getVoiceChannel(guild)
			val category = mainChannel.parentCategory ?: throw Exception("voice channel is not in a category")

			category.voiceChannels.forEach { channel ->
				if (channel.idLong == mainChannel.idLong) return@forEach

				val moveActions = channel.members.map { member ->
					guild.moveVoiceMember(member, mainChannel).submit()
				}

				CompletableFuture.supplyAsync {
					moveActions.forEach { future ->
						try {
							future.join()
						} catch (ex: Exception) {
							ex.printStackTrace()
						}
					}
					channel.delete().queue()
				}
			}

		} catch (ex: Exception) {
			ex.printStackTrace()
		}
	}

	fun getChannelName(uuid: UUID): String {
		val teamNumber = teamUuidToNumber[uuid]
		if (teamNumber == null) {
			val teamNumber = teamUuidToNumber.size + 1
			teamUuidToNumber[uuid] = teamNumber
			return "Team $teamNumber"
		} else {
			return "Team $teamNumber"
		}
	}

	fun addVoiceChannel(teamUuid: UUID, playerUuid: UUID) {
		try {
			val userId = config.playerToUser[playerUuid] ?: throw Exception("player is not mapped")

			val guild = uhcGuild
			val mainChannel = getVoiceChannel(guild)
			val category = mainChannel.parentCategory ?: throw Exception("voice channel is not in a category")

			val channelName = getChannelName(teamUuid)

			getOrCreateChannel(category, channelName).thenAccept { teamChannel ->
				guild.moveVoiceMember(UserSnowflake.fromId(userId), teamChannel).queue()
			}
		} catch (ex: Exception) {
			ex.printStackTrace()
		}
	}

	val uhcGuild: Guild
		get() = api.getGuildById(config.guildId) ?: throw Exception("UHC server guild missing")

	fun getVoiceChannel(guild: Guild): VoiceChannel =
		guild.getChannelById(
			VoiceChannel::class.java,
			config.voiceChannelId
				?: throw Exception("Voice channel config missing")
		) ?: throw Exception("Voice channel missing")

	override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
		if (event.name == "link" && event.focusedOption.name == "username") {
			event.replyChoiceStrings(Bukkit.getOnlinePlayers().filter { player ->
				!config.playerToUser.keys.contains(player.uniqueId)
			}.map { player -> player.name }).queue()
		} else if (event.name == "forcelink" && event.focusedOption.name == "username") {
			event.replyChoiceStrings(Bukkit.getOfflinePlayers().map { player -> player.name }).queue()
		}
	}

	override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
		if (event.name == "setsummarychannel") {
			event.deferReply().queue()

			val channel = event.getOption("channel")!!.asChannel

			config.summaryChannelId = channel.idLong
			Config.write(config)

			event.hook.sendMessage("Successfully set summary channel to ${channel.asMention}").queue()
		} else if (event.name == "setvoicechannel") {
			event.deferReply().queue()

			val channel = event.getOption("channel")!!.asChannel

			config.voiceChannelId = channel.idLong
			Config.write(config)

			event.hook.sendMessage("Successfully set voice channel to ${channel.asMention}").queue()
		} else if (event.name == "link") {
			event.deferReply().queue()

			val username = event.getOption("username")!!.asString
			val userId = event.user.idLong

			val player = Bukkit.getOnlinePlayers().find { player -> player.name == username }

			if (player == null) return event.hook.sendMessage("Could not find that player").queue()
			val mappedUser = config.playerToUser[player.uniqueId]

			if (mappedUser != null) {
				return if (mappedUser == userId) event.hook.sendMessage("You are already linked as ${player.name}")
					.queue()
				else event.hook.sendMessage("Someone else is already linked as ${player.name}").queue()
			}

			config.makeLink(player.uniqueId, userId)
			Config.write(config)

			event.hook.sendMessage("Successfully linked as ${player.name}").queue()
		} else if (event.name == "forcelink") {
			event.deferReply().queue()

			val user = event.getOption("user")!!.asUser
			val username = event.getOption("username")!!.asString

			val player = Bukkit.getOfflinePlayers().find { player -> player.name == username }
				?: return event.hook.sendMessage("Could not find that player").queue()

			config.makeLink(player.uniqueId, user.idLong)
			Config.write(config)

			event.hook.sendMessage("Successfully linked ${user.asMention} to ${player.name}").queue()
		} else if (event.name == "unlink") {
			event.deferReply().queue()

			val userId = event.user.idLong

			if (config.unlink(userId)) {
				Config.write(config)
				event.hook.sendMessage("Successfully unlinked").queue()
			} else {
				event.hook.sendMessage("You are not yet linked").queue()
			}
		} else if (event.name == "forceunlink") {
			event.deferReply().queue()

			val user = event.getOption("name")!!.asUser
			val userId = user.idLong

			if (config.unlink(userId)) {
				Config.write(config)
				event.hook.sendMessage("Successfully unlinked ${user.asMention}").queue()
			} else {
				event.hook.sendMessage("${user.asMention} is not linked").queue()
			}
		}
	}

	fun sendSummaryMessage(summary: Summary) {
		try {
			val guild = uhcGuild
			val summaryChannelId = config.summaryChannelId ?: return

			val channel = guild.getTextChannelById(summaryChannelId) ?: return

			channel.sendMessage(
				MessageCreateBuilder()
					.addEmbeds(SummaryMessage.create(summary))
					.build()
			).queue()
		} catch (ex: Exception) {
			ex.printStackTrace()
		}
	}

	companion object {
		var instance: GameRunnerBot? = null
			private set

		fun setup() {
			try {
				val config = Config.read()
					?: throw Exception("Could not load discord bot, could not load config file")

				val api = JDABuilder.createDefault(
					config.token,
					GatewayIntent.GUILD_MEMBERS,
					GatewayIntent.GUILD_MESSAGES,
					GatewayIntent.GUILD_VOICE_STATES,
					GatewayIntent.GUILD_MESSAGE_REACTIONS
				)
					.setStatus(OnlineStatus.ONLINE)
					.setActivity(Activity.playing("UHC"))
					.enableCache(CacheFlag.VOICE_STATE)
					.build()

				val bot = GameRunnerBot(api, config)
				CompletableFuture.runAsync {
					api.awaitReady()

					bot.init()

					instance = bot

					println("Loaded discord bot")
				}
			} catch (ex: Exception) {
				ex.printStackTrace()
			}
		}
	}
}
