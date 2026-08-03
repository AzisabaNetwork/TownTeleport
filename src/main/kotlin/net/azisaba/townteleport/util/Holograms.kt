package net.azisaba.townteleport.util

import net.azisaba.townteleport.data.TownTeleportData
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import java.util.UUID

object Holograms {
    private val shown = mutableMapOf<UUID, MutableMap<TownTeleportData, TextDisplay>>()

    fun updateAll(teleport: TownTeleportData) {
        shown.forEach { (uuid, map) ->
            val player = Bukkit.getPlayer(uuid)
            if (player == null || !player.isOnline) {
                shown.remove(uuid)
                return@forEach
            }
            map.remove(teleport)?.remove()
            show(teleport, player)
        }
    }

    fun show(teleport: TownTeleportData, player: Player) {
        val map = shown.computeIfAbsent(player.uniqueId) { mutableMapOf() }
        if (map.containsKey(teleport)) return
        val loc = teleport.location.clone().add(0.5, 1.5, 0.5)
        val world = loc.world ?: return
        val textDisplay = world.spawn(loc, TextDisplay::class.java) { display ->
            val text = listOf(
                "${ChatColor.LIGHT_PURPLE}✦ ${ChatColor.YELLOW}${teleport.name.colored()}",
                "${ChatColor.GOLD}使用コスト: ${ChatColor.YELLOW}${teleport.useCost.toReadableString()}",
                "${ChatColor.GOLD}テレポートコスト: ${ChatColor.YELLOW}${teleport.teleportCost.toReadableString()}",
                "${ChatColor.GOLD}クリックで使用"
            ).joinToString("\n")
            display.text = text
            display.billboard = Display.Billboard.CENTER
            display.isPersistent = false
            display.isSeeThrough = false
        }
        map[teleport] = textDisplay
    }

    fun hide(teleport: TownTeleportData, player: Player) {
        val map = shown[player.uniqueId] ?: return
        val display = map.remove(teleport) ?: return
        display.remove()
    }

    fun getAll(player: Player): List<TownTeleportData> {
        val map = shown[player.uniqueId] ?: return emptyList()
        return map.keys.toList()
    }

    fun hideAll(player: Player) {
        val map = shown.remove(player.uniqueId) ?: return
        map.values.forEach { it.remove() }
        map.clear()
    }
}
