package net.azisaba.townteleport.util

import org.bukkit.ChatColor

/**
 * Invokes [ChatColor.translateAlternateColorCodes] with `&` as the color code character.
 */
fun String.colored() = ChatColor.translateAlternateColorCodes('&', this)
