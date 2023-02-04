package net.azisaba.townteleport.listener

import com.palmergames.bukkit.towny.TownyAPI
import net.azisaba.townteleport.TownTeleport
import net.azisaba.townteleport.data.TownTeleportData
import net.azisaba.townteleport.gui.PortalScreen
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack
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
        if (e.itemInHand.type != Material.END_PORTAL_FRAME) return
        val nmsItem = CraftItemStack.asNMSCopy(e.itemInHand)
        val tag = nmsItem.tag ?: return
        if (!tag.hasKey("TownTeleportBlock")) return
        val townBlock = TownyAPI.getInstance().getTownBlock(e.blockPlaced.location)
        if (townBlock == null || !townBlock.hasTown()) {
            e.player.sendMessage("${ChatColor.RED}ここには設置できません。")
            e.isCancelled = true
            return
        }
        val town = townBlock.town
        if (town.mayor?.name != e.player.name) {
            e.player.sendMessage("${ChatColor.RED}ポータルを設置できるのは町長のみです。")
            e.isCancelled = true
            return
        }
        val name = plugin.dataConfig.findNextName(town.uuid)
        plugin.dataConfig.townTeleports.add(TownTeleportData(town.uuid, name, e.blockPlaced.location, 0.0, 0.0, mutableSetOf(), mutableSetOf()))
        e.player.sendMessage("${ChatColor.GREEN}テレポートポータル(${ChatColor.YELLOW}$name${ChatColor.GREEN})を設置しました。")
        plugin.saveAsync()
    }

    @EventHandler(ignoreCancelled = true)
    fun onPortalInteracted(e: PlayerInteractEvent) {
        if (e.hand != EquipmentSlot.HAND) return
        val clicked = e.clickedBlock
        if (clicked?.type != Material.END_PORTAL_FRAME) return
        val townBlock = TownyAPI.getInstance().getTownBlock(clicked.location)
        if (townBlock != null && !townBlock.hasTown()) return
        val town = townBlock.town
        val teleport = plugin.dataConfig.townTeleports.find { it.townId == town.uuid && it.location == clicked.location } ?: return
        e.isCancelled = true
        e.player.openInventory(PortalScreen(town, e.player, plugin, teleport).inv)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPortalBroken(e: BlockBreakEvent) {
        if (e.block.type != Material.END_PORTAL_FRAME) return
        val townBlock = TownyAPI.getInstance().getTownBlock(e.block.location)
        if (townBlock != null && !townBlock.hasTown()) return
        val town = townBlock.town
        val teleport = plugin.dataConfig.townTeleports.find { it.townId == town.uuid && it.location == e.block.location } ?: return
        e.isCancelled = true
        e.player.openInventory(PortalScreen(town, e.player, plugin, teleport).inv)
    }
}
