package net.azisaba.townteleport.event

import net.minecraft.server.v1_15_R1.BlockPosition
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a player tries to update a sign.
 * @param player The player who tried to update the sign.
 * @param pos The position of the sign.
 * @param lines The lines of the sign.
 */
data class AsyncPreSignChangeEvent(
    val player: Player,
    val pos: BlockPosition,
    val lines: List<String>,
) : Event(true) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        @get:JvmName("getHandlerList")
        val handlerList = HandlerList()
    }
}
