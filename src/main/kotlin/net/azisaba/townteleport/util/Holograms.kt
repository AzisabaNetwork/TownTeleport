package net.azisaba.townteleport.util

import net.azisaba.townteleport.data.TownTeleportData
import net.minecraft.server.v1_15_R1.ChatComponentText
import net.minecraft.server.v1_15_R1.EntityArmorStand
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntity
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.UUID

object Holograms {
    private fun createHologram(location: Location, text: String?): EntityArmorStand {
        val worldServer = (location.world!! as CraftWorld).handle
        val armorStand = EntityArmorStand(worldServer, location.x, location.y, location.z)
        armorStand.isInvisible = true
        armorStand.isInvulnerable = true
        armorStand.isNoGravity = true
        if (text != null) {
            armorStand.customNameVisible = true
            armorStand.customName = ChatComponentText(text)
        }
        return armorStand
    }

    private fun EntityArmorStand.spawn(player: Player): EntityArmorStand {
        (player as CraftPlayer).handle.playerConnection.apply {
            sendPacket(getSpawnPacket())
            sendPacket(getUpdatePacket())
        }
        return this
    }

    private fun EntityArmorStand.destroy(player: Player): EntityArmorStand {
        (player as CraftPlayer).handle.playerConnection.sendPacket(getDestroyPacket())
        return this
    }

    private fun EntityArmorStand.getSpawnPacket() = PacketPlayOutSpawnEntity(this)

    private fun EntityArmorStand.getDestroyPacket() = PacketPlayOutEntityDestroy(this.id)

    private fun EntityArmorStand.getUpdatePacket() = PacketPlayOutEntityMetadata(this.id, this.dataWatcher, true)

    private val shown = mutableMapOf<UUID, MutableMap<TownTeleportData, List<EntityArmorStand>>>()

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
        val teleportName = createHologram(teleport.location.clone().add(0.5, 0.75, 0.5), "${ChatColor.LIGHT_PURPLE}✦ ${ChatColor.YELLOW}${teleport.name.colored()}")
        teleportName.spawn(player)
        val useCost = createHologram(teleport.location.clone().add(0.5, 0.5, 0.5), "${ChatColor.GOLD}使用コスト: ${ChatColor.YELLOW}${teleport.useCost.toReadableString()}")
        useCost.spawn(player)
        val teleportCost = createHologram(teleport.location.clone().add(0.5, 0.25, 0.5), "${ChatColor.GOLD}テレポートコスト: ${ChatColor.YELLOW}${teleport.teleportCost.toReadableString()}")
        teleportCost.spawn(player)
        val clickToUse = createHologram(teleport.location.clone().add(0.5, 0.0, 0.5), "${ChatColor.GOLD}クリックで使用")
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
