package net.azisaba.townteleport.listener

import net.azisaba.townteleport.util.ChannelUtil
import net.azisaba.townteleport.util.Holograms
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object PlayerListener : Listener {
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        ChannelUtil.inject(e.player)
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        ChannelUtil.eject(e.player)
        Holograms.hideAll(e.player)
    }
}
