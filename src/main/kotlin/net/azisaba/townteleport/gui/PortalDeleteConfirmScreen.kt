package net.azisaba.townteleport.gui

import com.palmergames.bukkit.towny.`object`.Town
import net.azisaba.townteleport.TownTeleport
import net.azisaba.townteleport.util.colored
import net.azisaba.townteleport.data.TownTeleportData
import net.azisaba.townteleport.util.ItemUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class PortalDeleteConfirmScreen(
    val town: Town,
    val player: Player,
    val plugin: TownTeleport,
    val teleport: TownTeleportData,
) : AbstractScreen() {
    override val inv = Bukkit.createInventory(this, 27, "ポータル${ChatColor.YELLOW}${teleport.name.colored()}${ChatColor.RESET}を削除")

    init {
        for (i in 0..26) {
            inv.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }
        inv.setItem(11, createItem(Material.RED_TERRACOTTA, "${ChatColor.RED}削除する"))
        inv.setItem(15, createItem(Material.GREEN_TERRACOTTA, "${ChatColor.AQUA}キャンセル"))
    }

    object EventListener : Listener {
        @EventHandler
        fun onInventoryClick(e: InventoryClickEvent) {
            if (e.inventory.holder is PortalDeleteConfirmScreen) {
                e.isCancelled = true
            }
            val screen = e.clickedInventory?.holder
            if (screen !is PortalDeleteConfirmScreen) return
            if (e.slot == 11) {
                if (!screen.player.hasPermission("townteleport.admin") && screen.town.mayor.name != screen.player.name) {
                    screen.player.sendMessage("${ChatColor.RED}このテレポートポータルを削除する権限がありません。")
                    return
                }
                screen.plugin.dataConfig.townTeleports.remove(screen.teleport)
                screen.teleport.location.block.type = Material.AIR
                ItemUtil.createPortalItem().let {
                    screen.player.inventory.addItem(it).values.forEach { item ->
                        screen.player.world.dropItem(screen.player.location, item).let { itemEntity ->
                            itemEntity.isGlowing = true
                        }
                    }
                }
                screen.player.sendMessage("${ChatColor.GREEN}テレポートポータル(${ChatColor.YELLOW}${screen.teleport.name.colored()}${ChatColor.GREEN})を削除しました。")
                screen.player.closeInventory()
                screen.plugin.saveAsync()
            } else if (e.slot == 15) {
                e.whoClicked.closeInventory()
            }
        }
    }
}
