package net.azisaba.townteleport.gui

import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
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
            val loc0 = player.location.clone().apply { y = player.location.y.coerceAtLeast(1.0) }
            val origBlockData = loc0.block.blockData
            loc0.block.setBlockData(Material.OAK_SIGN.createBlockData(), false)
            val signState = loc0.block.state as? Sign
            awaitingSign[player.uniqueId] = { lines ->
                loc0.block.setBlockData(origBlockData, false)
                action(lines)
            }
            if (signState != null) {
                player.openSign(signState)
            } else {
                loc0.block.setBlockData(origBlockData, false)
            }
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
        fun onSignChange(e: SignChangeEvent) {
            val action = awaitingSign.remove(e.player.uniqueId) ?: return
            e.isCancelled = true
            action(e.lines.filterNotNull())
        }
    }
}
