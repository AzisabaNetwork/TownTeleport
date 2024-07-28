package net.azisaba.townteleport.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

object ItemUtil {
    fun createPortalItem(): ItemStack =
        ItemStack(Material.END_PORTAL_FRAME).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text("テレポーター", NamedTextColor.LIGHT_PURPLE))
                lore(listOf(
                    Component.text("自分が町長になっている町の敷地に設置すると", NamedTextColor.GREEN),
                    Component.text("「テレポートポータル」が設置できます。", NamedTextColor.GREEN))
                )
            }
        }.let {
            val nms = CraftItemStack.asNMSCopy(it)
            nms.orCreateTag.putBoolean("TownTeleportBlock", true)
            CraftItemStack.asCraftMirror(nms)
        }
}
