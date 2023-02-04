package net.azisaba.townteleport.util

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

object ItemUtil {
    fun createPortalItem(): ItemStack =
        ItemStack(Material.END_PORTAL_FRAME).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName("${ChatColor.LIGHT_PURPLE}テレポーター")
                lore = listOf("${ChatColor.GREEN}自分が町長になっている町の敷地に設置すると", "${ChatColor.GREEN}「テレポートポータル」が設置できます。")
            }
        }.let {
            val nms = CraftItemStack.asNMSCopy(it)
            nms.orCreateTag.setBoolean("TownTeleportBlock", true)
            CraftItemStack.asCraftMirror(nms)
        }
}
