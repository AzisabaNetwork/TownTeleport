package net.azisaba.townteleport.network

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.azisaba.townteleport.event.AsyncPreSignChangeEvent
import net.minecraft.server.v1_15_R1.PacketPlayInUpdateSign
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PacketHandler(private val player: Player) : ChannelDuplexHandler() {
    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        if (msg is PacketPlayInUpdateSign) {
            val lines = msg.c().toList()
            Bukkit.getPluginManager().callEvent(AsyncPreSignChangeEvent(player, msg.b(), lines))
        }
        super.channelRead(ctx, msg)
    }
}
