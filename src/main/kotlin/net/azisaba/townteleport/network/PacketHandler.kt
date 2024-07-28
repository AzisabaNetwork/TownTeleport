package net.azisaba.townteleport.network

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.azisaba.townteleport.event.AsyncPreSignChangeEvent
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PacketHandler(private val player: Player) : ChannelDuplexHandler() {
    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        if (msg is ServerboundSignUpdatePacket) {
            val lines = msg.lines.toList()
            Bukkit.getPluginManager().callEvent(AsyncPreSignChangeEvent(player, msg.pos, lines))
        }
        super.channelRead(ctx, msg)
    }
}
