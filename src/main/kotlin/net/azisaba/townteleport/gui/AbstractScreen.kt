package net.azisaba.townteleport.gui

import net.azisaba.townteleport.event.AsyncPreSignChangeEvent
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractScreen : InventoryHolder {
    companion object {
        private val awaitingSign = ConcurrentHashMap<UUID, (List<String>) -> Unit>()

        fun promptSign(player: Player, action: (List<String>) -> Unit) {
            val loc0 = player.location.clone().apply { y = 0.0 }
            val origBlockData = loc0.block.blockData
            player.sendBlockChange(loc0, Material.AIR.createBlockData())
            player.sendBlockChange(loc0, Material.OAK_SIGN.createBlockData())
            awaitingSign[player.uniqueId] = {
                player.sendBlockChange(loc0, origBlockData)
                action(it)
            }
            (player as CraftPlayer).handle.connection
                .send(ClientboundOpenSignEditorPacket(BlockPos(loc0.blockX, loc0.blockY, loc0.blockZ), true))
        }
    }

    open val cancelInventoryDragEvent = true
    abstract val inv: Inventory

    override fun getInventory(): Inventory = inv

    fun createItem(type: Material, name: String, lore: List<String> = listOf(), amount: Int = 1, action: ItemStack.() -> Unit = {}) =
        ItemStack(type, amount).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName(name)
                setLore(lore)
            }
            action()
        }

    object EventListener : Listener {
        @EventHandler
        fun onInventoryDrag(e: InventoryDragEvent) {
            val screen = e.inventory.holder
            if (screen is AbstractScreen && screen.cancelInventoryDragEvent) {
                e.isCancelled = true
            }
        }

        @EventHandler
        fun onSignChange(e: AsyncPreSignChangeEvent) {
            val action = awaitingSign.remove(e.player.uniqueId) ?: return
            action(e.lines)
        }
    }
}
