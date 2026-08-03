package net.azisaba.townteleport.listener

import net.azisaba.townteleport.util.Holograms
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

object PlayerListener : Listener {
    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        Holograms.hideAll(e.player)
    }
}
