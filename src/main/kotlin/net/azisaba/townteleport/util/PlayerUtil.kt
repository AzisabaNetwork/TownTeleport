package net.azisaba.townteleport.util

import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.plugin.Plugin

object PlayerUtil {
    lateinit var plugin: Plugin

    fun HumanEntity.closeInventoryLater() {
        Bukkit.getScheduler().runTaskLater(plugin, Runnable { closeInventory() }, 1)
    }
}
