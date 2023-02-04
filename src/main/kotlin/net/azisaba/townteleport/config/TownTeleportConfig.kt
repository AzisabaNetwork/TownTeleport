package net.azisaba.townteleport.config

import org.bukkit.configuration.ConfigurationSection

// TODO: unused
class TownTeleportConfig(private val config: ConfigurationSection) {
    val enabledWorlds: List<String> = config.getStringList("enabled-worlds")
}
