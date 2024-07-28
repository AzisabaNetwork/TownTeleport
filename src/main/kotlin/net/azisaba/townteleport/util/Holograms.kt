package net.azisaba.townteleport.util

import net.azisaba.townteleport.data.TownTeleportData
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.decoration.ArmorStand
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

object Holograms {
    private fun createHologram(location: Location, text: String?): ArmorStand {
        val worldServer = (location.world!! as CraftWorld).handle
        val armorStand = ArmorStand(worldServer, location.x, location.y, location.z)
        armorStand.isInvisible = true
        armorStand.isInvulnerable = true
        armorStand.isNoGravity = true
        armorStand.isSmall = true
        if (text != null) {
            armorStand.isCustomNameVisible = true
            armorStand.customName = Component.literal(text)
        }
        return armorStand
    }

    private fun ArmorStand.spawn(player: Player): ArmorStand {
        (player as CraftPlayer).handle.connection.apply {
            send(getSpawnPacket())
            getUpdatePacket()?.let { send(it) }
        }
        return this
    }

    private fun ArmorStand.destroy(player: Player): ArmorStand {
        (player as CraftPlayer).handle.connection.send(getDestroyPacket())
        return this
    }

    private fun ArmorStand.getSpawnPacket() = ClientboundAddEntityPacket(this)

    private fun ArmorStand.getDestroyPacket() = ClientboundRemoveEntitiesPacket(this.id)

    private fun ArmorStand.getUpdatePacket() =
        this.entityData.nonDefaultValues?.let { ClientboundSetEntityDataPacket(this.id, it) }

    private val shown = mutableMapOf<UUID, MutableMap<TownTeleportData, List<ArmorStand>>>()

    fun updateAll(teleport: TownTeleportData) {
        shown.forEach { (uuid, map) ->
            val player = Bukkit.getPlayer(uuid)
            if (player == null || !player.isOnline) {
                shown.remove(uuid)
                return@forEach
            }
            map.remove(teleport)?.forEach { it.destroy(player) }
            show(teleport, player)
        }
    }

    fun show(teleport: TownTeleportData, player: Player) {
        val map = shown.computeIfAbsent(player.uniqueId) { mutableMapOf() }
        if (map.containsKey(teleport)) return
        val teleportName = createHologram(teleport.location.clone().add(0.5, 1.75, 0.5), "§d✦ §e${teleport.name.colored()}")
        teleportName.spawn(player)
        val useCost = createHologram(teleport.location.clone().add(0.5, 1.5, 0.5), "§6使用コスト: §e${teleport.useCost.toReadableString()}")
        useCost.spawn(player)
        val teleportCost = createHologram(teleport.location.clone().add(0.5, 1.25, 0.5), "§6テレポートコスト: §e${teleport.teleportCost.toReadableString()}")
        teleportCost.spawn(player)
        val clickToUse = createHologram(teleport.location.clone().add(0.5, 1.0, 0.5), "§6クリックで使用")
        clickToUse.spawn(player)
        map[teleport] = listOf(teleportName, useCost, teleportCost, clickToUse)
    }

    fun hide(teleport: TownTeleportData, player: Player) {
        val map = shown[player.uniqueId] ?: return
        val list = map[teleport] ?: return
        list.forEach { it.destroy(player) }
        map.remove(teleport)
    }

    fun getAll(player: Player): List<TownTeleportData> {
        val map = shown[player.uniqueId] ?: return emptyList()
        return map.keys.toList()
    }

    fun hideAll(player: Player) {
        val map = shown[player.uniqueId] ?: return
        if (player.isOnline) {
            map.values.forEach { list -> list.forEach { it.destroy(player) } }
        }
        map.clear()
    }
}
