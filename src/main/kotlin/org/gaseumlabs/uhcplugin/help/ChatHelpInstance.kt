package org.gaseumlabs.uhcplugin.help

data class RequirementInstace(val requirement: ChatHelpRequirement, var found: Boolean)

data class ChatHelpInstance(val item: ChatHelpItem, val requirements: ArrayList<RequirementInstace>, var found: Boolean)
