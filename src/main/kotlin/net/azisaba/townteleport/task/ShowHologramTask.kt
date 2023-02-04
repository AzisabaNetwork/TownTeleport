package net.azisaba.townteleport.task

import net.azisaba.townteleport.TownTeleport
import net.azisaba.townteleport.util.Holograms
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class ShowHologramTask(private val plugin: TownTeleport) : BukkitRunnable() {
    override fun run() {
        Bukkit.getOnlinePlayers().forEach { player -> Holograms.hideAll(player) }
        plugin.dataConfig.townTeleports.forEach { teleport ->
            teleport.location.world!!.getNearbyEntities(teleport.location, 50.0, 50.0, 50.0) { it is Player }.forEach { player ->
                if (player is Player) {
                    // show hologram if player is near teleport
                    Holograms.show(teleport, player)
                }
            }
        }
    }
}
