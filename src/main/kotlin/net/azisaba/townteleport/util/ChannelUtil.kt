package net.azisaba.townteleport.util

import io.netty.channel.Channel
import net.azisaba.townteleport.network.PacketHandler
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer
import org.bukkit.entity.Player

object ChannelUtil {
    private fun getChannel(player: Player): Channel? = (player as CraftPlayer).handle.playerConnection.networkManager.channel

    /**
     * Injects a [PacketHandler] into the player's channel.
     */
    fun inject(player: Player) {
        val channel = getChannel(player) ?: return
        if (channel.pipeline().get("town_teleport") != null) return
        channel.pipeline().addBefore("packet_handler", "town_teleport", PacketHandler(player))
    }

    /**
     * Ejects the [PacketHandler] from the player's channel.
     */
    fun eject(player: Player) {
        try {
            val channel = getChannel(player) ?: return
            channel.pipeline().remove("town_teleport")
        } catch (ignored: Exception) {}
    }
}
