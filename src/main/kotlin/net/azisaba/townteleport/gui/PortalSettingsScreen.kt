package net.azisaba.townteleport.gui

import net.azisaba.townteleport.TownTeleport
import net.azisaba.townteleport.data.ModifyPermissiveTarget
import net.azisaba.townteleport.data.TownTeleportData
import net.azisaba.townteleport.util.Holograms
import net.azisaba.townteleport.util.PlayerUtil.closeInventoryLater
import net.azisaba.townteleport.util.colored
import net.azisaba.townteleport.util.parseNumber
import net.azisaba.townteleport.util.toReadableString
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import kotlin.math.roundToLong

class PortalSettingsScreen(
    val plugin: TownTeleport,
    val player: Player,
    private val playerIsMayor: Boolean,
    val teleport: TownTeleportData,
) : AbstractScreen() {
    override val inv = Bukkit.createInventory(this, 9 * 6, "テレポートポータルの設定")

    init {
        update()
    }

    fun update() {
        for (i in 0..8) {
            inv.setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, " "))
        }
        inv.setItem(9, createItem(Material.BLACK_STAINED_GLASS_PANE, " "))
        inv.setItem(17, createItem(Material.BLACK_STAINED_GLASS_PANE, " "))
        inv.setItem(18, createItem(Material.BLACK_STAINED_GLASS_PANE, " "))
        inv.setItem(26, createItem(Material.BLACK_STAINED_GLASS_PANE, " "))
        inv.setItem(27, createItem(Material.BLACK_STAINED_GLASS_PANE, " "))
        inv.setItem(35, createItem(Material.BLACK_STAINED_GLASS_PANE, " "))
        inv.setItem(36, createItem(Material.BLACK_STAINED_GLASS_PANE, " "))
        inv.setItem(44, createItem(Material.BLACK_STAINED_GLASS_PANE, " "))
        for (i in 45..53) {
            inv.setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, " "))
        }
        inv.setItem(10, createItem(
            Material.PAPER,
            "${ChatColor.GOLD}テレポートポータルの名前を変更する",
            listOf("${ChatColor.GOLD}現在の名前: ${ChatColor.YELLOW}${teleport.name.colored()}"),
        ))
        val modifyLore = mutableListOf<String>()
        modifyLore.add("${ChatColor.GOLD}現在の設定: ${ChatColor.YELLOW}${teleport.modifyPermission.joinToString(", ")}")
        if (!playerIsMayor) {
            modifyLore.add("")
            modifyLore.add("${ChatColor.RED}この設定を変更する権限がありません。")
        }
        inv.setItem(11, createItem(
            if (playerIsMayor) Material.GOLDEN_AXE else Material.BARRIER,
            "${ChatColor.GOLD}設定を変更できるプレイヤーを変更する",
            modifyLore
        ))
        inv.setItem(12, createItem(
            Material.ENDER_PEARL,
            "${ChatColor.GOLD}テレポートできるプレイヤーを変更する",
            listOf("${ChatColor.GOLD}現在の設定: ${ChatColor.YELLOW}${teleport.teleportPermission.joinToString(", ")}")
        ))
        inv.setItem(13, createItem(
            Material.PAPER,
            "${ChatColor.GOLD}テレポートのコストを変更する",
            listOf("${ChatColor.GOLD}現在の設定: ${ChatColor.YELLOW}${teleport.teleportCost.toReadableString()}")
        ))
        inv.setItem(14, createItem(
            Material.PAPER,
            "${ChatColor.GOLD}テレポートポータルの使用コストを変更する",
            listOf("${ChatColor.GOLD}現在の設定: ${ChatColor.YELLOW}${teleport.useCost.toReadableString()}")
        ))
        inv.setItem(49, createItem(Material.BARRIER, "${ChatColor.RED}閉じる"))
    }

    object EventListener : Listener {
        @EventHandler
        fun onInventoryClick(e: InventoryClickEvent) {
            if (e.inventory.holder is PortalSettingsScreen) {
                e.isCancelled = true
            }
            val screen = e.clickedInventory?.holder
            if (screen !is PortalSettingsScreen) return
            if (e.slot == 10) {
                promptSign(screen.player) {
                    val joined = it.joinToString("")
                    if (joined.isNotBlank()) {
                        screen.teleport.name = joined
                    }
                    Bukkit.getScheduler().runTask(screen.plugin, Runnable {
                        if (joined.isNotBlank()) {
                            screen.player.sendMessage("${ChatColor.GREEN}テレポートポータルの名前を変更しました。")
                        }
                        screen.update()
                        screen.player.openInventory(screen.inv)
                    })
                }
            } else if (e.slot == 11 && e.currentItem?.type == Material.GOLDEN_AXE) {
                if (screen.teleport.modifyPermission.contains(ModifyPermissiveTarget.Resident)) {
                    screen.teleport.modifyPermission.clear()
                } else if (screen.teleport.modifyPermission.contains(ModifyPermissiveTarget.Assistant)) {
                    screen.teleport.modifyPermission.add(ModifyPermissiveTarget.Resident)
                } else {
                    screen.teleport.modifyPermission.add(ModifyPermissiveTarget.Assistant)
                }
                screen.player.sendMessage("${ChatColor.GREEN}設定を変更できるプレイヤーを変更しました。")
                screen.player.playSound(screen.player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
                screen.update()
            } else if (e.slot == 12) {
                screen.player.openInventory(PortalTeleportPermissionScreen(screen).inventory)
            } else if (e.slot == 13) {
                promptSign(screen.player) {
                    val cost = it.joinToString("").parseNumber()
                    if (cost == null) {
                        screen.player.sendMessage("${ChatColor.RED}数値をパースできませんでした。")
                    } else {
                        screen.teleport.teleportCost = (cost * 100.0).roundToLong() / 100.0
                        Bukkit.getScheduler().runTask(screen.plugin, Runnable {
                            screen.player.sendMessage("${ChatColor.GREEN}テレポートに必要なコストを変更しました。 ${ChatColor.GRAY}(${screen.teleport.teleportCost.toReadableString()})")
                            screen.update()
                            screen.player.openInventory(screen.inv)
                        })
                    }
                }
            } else if (e.slot == 14) {
                promptSign(screen.player) {
                    val cost = it.joinToString("").parseNumber()
                    if (cost == null) {
                        screen.player.sendMessage("${ChatColor.RED}数値をパースできませんでした。")
                    } else {
                        screen.teleport.useCost = (cost * 100.0).roundToLong() / 100.0
                        Bukkit.getScheduler().runTask(screen.plugin, Runnable {
                            screen.player.sendMessage("${ChatColor.GREEN}テレポートポータルの使用に必要なコストを変更しました。 ${ChatColor.GRAY}(${screen.teleport.useCost.toReadableString()})")
                            screen.update()
                            screen.player.openInventory(screen.inv)
                        })
                    }
                }
            } else if (e.slot == 49) {
                e.whoClicked.closeInventoryLater()
            }
        }
    }
}