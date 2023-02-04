package net.azisaba.townteleport.gui

import com.palmergames.bukkit.towny.`object`.Town
import net.azisaba.townteleport.TownTeleport
import net.azisaba.townteleport.util.colored
import net.azisaba.townteleport.data.TownTeleportData
import net.azisaba.townteleport.util.toReadableString
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import kotlin.math.min

class PortalScreen(
    private val town: Town,
    private val player: Player,
    private val plugin: TownTeleport,
    private val interactedTeleport: TownTeleportData,
) : AbstractScreen() {
    override val inv = Bukkit.createInventory(this, 54, "テレポートポータル")
    private var page: Int = 0
    private val teleports =
        plugin
            .dataConfig
            .townTeleports
            .filter { it.townId == town.uuid && it.hasPermissionToTeleport(town, player) }
            .sortedBy { it.name }

    init {
        update()
    }

    fun update() {
        for (i in 0..44) {
            inv.setItem(i, createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " "))
        }
        for (i in 45..53) {
            inv.setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, " "))
        }
        teleports.subList(page * 45, min(teleports.size, (page + 1) * 45)).forEachIndexed { index, teleport ->
            val canModify = player.hasPermission("townteleport.admin") || teleport.hasPermissionToModify(town.residents.find { it.name == player.name })
            val lore = mutableListOf<String>()
            teleport.location.apply {
                lore.add("${ChatColor.GOLD}テレポート先: ${ChatColor.GREEN}${world?.name}, $blockX, $blockY, $blockZ")
            }
            val totalCost = interactedTeleport.useCost + teleport.teleportCost
            if (canModify && totalCost > 0) {
                lore.add("${ChatColor.GOLD}コスト: ${ChatColor.DARK_GRAY}${ChatColor.STRIKETHROUGH}${totalCost.toReadableString()}${ChatColor.YELLOW} 0.0")
            } else {
                lore.add("${ChatColor.GOLD}コスト: ${ChatColor.YELLOW}${totalCost.toReadableString()}")
            }
            lore.add("")
            if (canModify) {
                lore.add("${ChatColor.YELLOW}✦ 左クリックでテレポート")
                lore.add("${ChatColor.YELLOW}✎ 右クリックで編集")
            } else {
                lore.add("${ChatColor.YELLOW}✦ クリックでテレポート")
            }
            inv.setItem(index, createItem(Material.ENDER_PEARL, "${ChatColor.GREEN}${teleport.name.colored()}", lore))
        }
        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, "${ChatColor.GOLD}前のページ"))
        }
        inv.setItem(49, createItem(Material.BARRIER, "${ChatColor.RED}閉じる"))
        if (player.hasPermission("townteleport.admin") || town.mayor.name == player.name) {
            inv.setItem(52, createItem(Material.REDSTONE_BLOCK, "${ChatColor.RED}テレポートポータルを削除"))
        }
        if (teleports.size > (page + 1) * 45) {
            inv.setItem(53, createItem(Material.ARROW, "${ChatColor.GOLD}次のページ"))
        }
    }

    object EventListener : Listener {
        @Suppress("DEPRECATION")
        @EventHandler
        fun onInventoryClick(e: InventoryClickEvent) {
            if (e.inventory.holder is PortalScreen) {
                e.isCancelled = true
            }
            val screen = e.clickedInventory?.holder
            if (screen !is PortalScreen) return
            val clickedItem = e.currentItem ?: return
            when (e.slot) {
                in 0..44 -> {
                    if (clickedItem.type != Material.ENDER_PEARL) return
                    val teleport = screen.teleports[screen.page * 45 + e.slot]
                    val player = e.whoClicked as Player
                    val canModify = player.hasPermission("townteleport.admin") || teleport.hasPermissionToModify(screen.town.residents.find { it.name == player.name })
                    if (e.click.isLeftClick || !canModify) {
                        val location = teleport.location.clone()
                        location.yaw = player.location.yaw
                        location.pitch = player.location.pitch
                        if (!location.add(0.5, 1.0, 0.5).block.type.isAir || !location.clone().add(
                                0.0,
                                1.0,
                                0.0
                            ).block.type.isAir
                        ) {
                            player.sendMessage("${ChatColor.RED}テレポート先が塞がっているため、テレポートできません。")
                            player.closeInventory()
                            return
                        }
                        val totalCost = screen.interactedTeleport.useCost + teleport.teleportCost
                        if (!canModify && totalCost > 0) {
                            // collect cost
                            val economy = Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider
                            economy?.getBalance(player)?.let {
                                if (it < totalCost) {
                                    player.sendMessage("${ChatColor.RED}テレポートに必要なお金(${ChatColor.YELLOW}${totalCost.toReadableString()}${ChatColor.RED})が足りません。")
                                    player.closeInventory()
                                    return
                                }
                            }
                            economy?.withdrawPlayer(player, totalCost)
                            screen.town.account.collect(totalCost * 0.1, "Teleport to ${teleport.name} by ${player.name}")
                        }
                        player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f)
                        screen.plugin.logger.info("${player.name} (${player.uniqueId}) teleported to ${teleport.name} (${teleport.location})")
                        screen.plugin.logger.info("Collected ${totalCost.toReadableString()} from ${player.name} (${player.uniqueId}) and paid ${totalCost * 0.1} to town ${screen.town.name}")
                        player.closeInventory()
                        player.teleport(location)
                        if (!canModify && totalCost > 0) {
                            player.sendMessage("${ChatColor.GREEN}${totalCost.toReadableString()}を支払い、${ChatColor.YELLOW}${teleport.name.colored()}${ChatColor.GREEN}にテレポートしました。")
                        } else {
                            player.sendMessage("${ChatColor.YELLOW}${teleport.name.colored()}${ChatColor.GREEN}にテレポートしました。")
                        }
                    } else {
                        val isMayor = screen.town.mayor.name == screen.player.name
                        screen.player.openInventory(PortalSettingsScreen(screen.plugin, player, isMayor, teleport).inventory)
                    }
                }
                45 -> {
                    // back
                    if (clickedItem.type == Material.ARROW) {
                        screen.page--
                        screen.update()
                    }
                }
                49 -> {
                    // close
                    if (clickedItem.type == Material.BARRIER) {
                        screen.player.closeInventory()
                    }
                }
                52 -> {
                    // delete
                    if (clickedItem.type == Material.REDSTONE_BLOCK) {
                        screen.player.openInventory(
                            PortalDeleteConfirmScreen(screen.town, screen.player, screen.plugin, screen.interactedTeleport).inventory
                        )
                    }
                }
                53 -> {
                    // next
                    if (clickedItem.type == Material.ARROW) {
                        screen.page++
                        screen.update()
                    }
                }
            }
        }
    }
}
