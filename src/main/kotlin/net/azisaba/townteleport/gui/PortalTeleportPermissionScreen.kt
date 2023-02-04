package net.azisaba.townteleport.gui

import net.azisaba.townteleport.data.TeleportPermissiveTarget
import net.azisaba.townteleport.util.toggle
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class PortalTeleportPermissionScreen(val settings: PortalSettingsScreen) : AbstractScreen() {
    override val inv = Bukkit.createInventory(this, 9 * 3, "テレポート権限の設定")

    init {
        update()
    }

    fun update() {
        for (i in 0..26) {
            inv.setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, " "))
        }
        setSlot(10, TeleportPermissiveTarget.Resident)
        setSlot(12, TeleportPermissiveTarget.Nation)
        setSlot(14, TeleportPermissiveTarget.Ally)
        setSlot(16, TeleportPermissiveTarget.Outsider)
        inv.setItem(22, createItem(Material.ARROW, "${ChatColor.YELLOW}戻る"))
    }

    private fun setSlot(slot: Int, target: TeleportPermissiveTarget) {
        inv.setItem(slot, createItem(
            if (settings.teleport.teleportPermission.contains(target)) {
                Material.LIME_WOOL
            } else {
                Material.RED_WOOL
            },
            if (settings.teleport.teleportPermission.contains(target)) {
                ChatColor.GREEN
            } else {
                ChatColor.RED
            }.toString() + target.name,
            target.description.split("\n").map { "${ChatColor.YELLOW}$it" }
        ))
    }

    object EventListener : Listener {
        @EventHandler
        fun onInventoryClick(e: InventoryClickEvent) {
            if (e.inventory.holder is PortalTeleportPermissionScreen) {
                e.isCancelled = true
            }
            val screen = e.clickedInventory?.holder
            if (screen !is PortalTeleportPermissionScreen) return
            when (e.slot) {
                10 -> screen.settings.teleport.teleportPermission.toggle(TeleportPermissiveTarget.Resident)
                12 -> screen.settings.teleport.teleportPermission.toggle(TeleportPermissiveTarget.Nation)
                14 -> screen.settings.teleport.teleportPermission.toggle(TeleportPermissiveTarget.Ally)
                16 -> screen.settings.teleport.teleportPermission.toggle(TeleportPermissiveTarget.Outsider)
                22 -> {
                    screen.settings.update()
                    screen.settings.player.openInventory(screen.settings.inventory)
                    return
                }
            }
            if (e.slot == 10 || e.slot == 12 || e.slot == 14 || e.slot == 16) {
                screen.settings.player.playSound(screen.settings.player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
                screen.update()
                screen.settings.plugin.saveAsync()
            }
        }
    }
}