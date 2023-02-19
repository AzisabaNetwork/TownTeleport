package net.azisaba.townteleport.listener

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

object NoPortalListener : Listener {
    @EventHandler
    fun onPlacePortal(e: BlockPlaceEvent) {
        if (e.player.hasPermission("townteleport.admin") && e.player.gameMode == GameMode.CREATIVE) {
            return
        }
        if (e.block.type == Material.END_PORTAL_FRAME) {
            e.isCancelled = true
        }
    }
}
