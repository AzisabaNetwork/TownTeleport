package net.azisaba.townteleport.util

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

object ItemUtil {
    fun createPortalItem(plugin: Plugin): ItemStack =
        ItemStack(Material.END_PORTAL_FRAME).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName("${ChatColor.LIGHT_PURPLE}テレポーター")
                lore = listOf("${ChatColor.GREEN}自分が町長になっている町の敷地に設置すると", "${ChatColor.GREEN}「テレポートポータル」が設置できます。")
                val key = NamespacedKey(plugin, "town_teleport_block")
                persistentDataContainer.set(key, PersistentDataType.BYTE, 1.toByte())
            }
        }

    fun isPortalItem(plugin: Plugin, item: ItemStack?): Boolean {
        if (item == null || item.type != Material.END_PORTAL_FRAME) return false
        val meta = item.itemMeta ?: return false
        val key = NamespacedKey(plugin, "town_teleport_block")
        return meta.persistentDataContainer.has(key, PersistentDataType.BYTE)
    }
}
