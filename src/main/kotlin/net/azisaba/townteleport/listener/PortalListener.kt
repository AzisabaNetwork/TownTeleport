package net.azisaba.townteleport.listener

import net.azisaba.townia.api.TowniaAPI
import net.azisaba.townteleport.TownTeleport
import net.azisaba.townteleport.data.TownTeleportData
import net.azisaba.townteleport.gui.PortalScreen
import net.azisaba.townteleport.util.LocationUtil.equalsBlockPos
import net.azisaba.townteleport.util.ItemUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class PortalListener(private val plugin: TownTeleport) : Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPortalPlaced(e: BlockPlaceEvent) {
        if (!ItemUtil.isPortalItem(plugin, e.itemInHand)) return
        val api = TowniaAPI.get() ?: return
        val plot = api.getPlot(e.blockPlaced.chunk).orElse(null)
        if (plot == null || plot.townUuid == null) {
            e.player.sendMessage("${ChatColor.RED}ここには設置できません。")
            e.isCancelled = true
            return
        }
        val town = api.getTown(plot.townUuid!!).orElse(null)
        if (town == null || town.id == null) {
            e.player.sendMessage("${ChatColor.RED}ここには設置できません。")
            e.isCancelled = true
            return
        }
        if (town.mayorUuid != e.player.uniqueId && !e.player.hasPermission("townteleport.admin")) {
            e.player.sendMessage("${ChatColor.RED}ポータルを設置できるのは町長のみです。")
            e.isCancelled = true
            return
        }
        val name = plugin.dataConfig.findNextName(town.id!!)
        plugin.dataConfig.townTeleports.add(TownTeleportData(town.id!!, name, e.blockPlaced.location, 0.0, 0.0, mutableSetOf(), mutableSetOf()))
        e.player.sendMessage("${ChatColor.GREEN}テレポートポータル(${ChatColor.YELLOW}$name${ChatColor.GREEN})を設置しました。")
        plugin.saveAsync()
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockPlaceOnTopOfPortal(e: BlockPlaceEvent) {
        val below = e.blockPlaced.location.clone().subtract(0.0, 1.0, 0.0).block
        val api = TowniaAPI.get() ?: return
        val plot = api.getPlot(below.chunk).orElse(null) ?: return
        val townUuid = plot.townUuid ?: return
        if (plugin.dataConfig.townTeleports.any { it.townId == townUuid && it.location.equalsBlockPos(below.location) }) {
            e.isCancelled = true
            e.player.sendMessage("${ChatColor.RED}ここにブロックは置けません。")
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPortalInteracted(e: PlayerInteractEvent) {
        if (e.hand != EquipmentSlot.HAND) return
        val clicked = e.clickedBlock
        if (clicked?.type != Material.END_PORTAL_FRAME) return
        val api = TowniaAPI.get() ?: return
        val plot = api.getPlot(clicked.chunk).orElse(null) ?: return
        val townUuid = plot.townUuid ?: return
        val town = api.getTown(townUuid).orElse(null) ?: return
        val teleport = plugin.dataConfig.townTeleports.find { it.townId == townUuid && it.location == clicked.location } ?: return
        e.isCancelled = true
        e.player.openInventory(PortalScreen(town, e.player, plugin, teleport).inv)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPortalBroken(e: BlockBreakEvent) {
        if (e.block.type != Material.END_PORTAL_FRAME) return
        val api = TowniaAPI.get() ?: return
        val plot = api.getPlot(e.block.chunk).orElse(null) ?: return
        val townUuid = plot.townUuid ?: return
        val town = api.getTown(townUuid).orElse(null) ?: return
        val teleport = plugin.dataConfig.townTeleports.find { it.townId == townUuid && it.location == e.block.location } ?: return
        e.isCancelled = true
        e.player.openInventory(PortalScreen(town, e.player, plugin, teleport).inv)
    }
}
