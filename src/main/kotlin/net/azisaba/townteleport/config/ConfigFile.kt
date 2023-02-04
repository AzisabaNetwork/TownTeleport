package net.azisaba.townteleport.config

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object ConfigFile {
    fun load(file: File) = YamlConfiguration.loadConfiguration(file)

    fun save(file: File, config: YamlConfiguration) = config.save(file)
}
