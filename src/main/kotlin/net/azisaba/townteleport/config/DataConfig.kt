package net.azisaba.townteleport.config

import net.azisaba.townteleport.data.ModifyPermissiveTarget
import net.azisaba.townteleport.data.TeleportPermissiveTarget
import net.azisaba.townteleport.data.TownTeleportData
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import java.util.UUID

class DataConfig(val config: YamlConfiguration) {
    val townTeleports: MutableList<TownTeleportData> = config.getKeys(false).mapNotNull<String?, List<TownTeleportData>> { rawTownId ->
        rawTownId!!
        val townId = UUID.fromString(rawTownId)
        config.getList(rawTownId)?.map { obj ->
            try {
                val map = obj as Map<*, *>
                if (map["town-id"] as String != rawTownId) {
                    throw RuntimeException("Town ID mismatch: $townId != ${map["town-id"]}")
                }
                val name = map["name"] as String
                val rawLocation = map["location"] as String
                val location = rawLocation.split(";").let {
                    Location(
                        Bukkit.getWorld(it[0]),
                        it[1].toDouble(),
                        it[2].toDouble(),
                        it[3].toDouble(),
                    )
                }
                val useCost = (map["use-cost"] as Number?)?.toDouble() ?: 0.0
                val teleportCost = (map["teleport-cost"] as Number?)?.toDouble() ?: 0.0
                val teleportPermission =
                    (map["teleport-permission"] as List<*>)
                        .map { it.toString() }
                        .map { it.replaceFirstChar { c -> c.uppercase() } }
                        .map { TeleportPermissiveTarget.valueOf(it) }
                        .toMutableSet()
                val modifyPermission =
                    (map["modify-permission"] as List<*>)
                        .map { it.toString() }
                        .map { it.replaceFirstChar { c -> c.uppercase() } }
                        .map { ModifyPermissiveTarget.valueOf(it) }
                        .toMutableSet()
                TownTeleportData(townId, name, location, useCost, teleportCost, teleportPermission, modifyPermission)
            } catch (e: Exception) {
                throw RuntimeException("Failed to load town teleport data: $townId", e)
            }
        }
    }.flatten().toMutableList()

    fun findNextName(townId: UUID): String {
        val townTeleports = townTeleports.filter { it.townId == townId }
        val max = townTeleports
            .map { it.name }
            .map { it.replace(Regex("テレポート #(\\d+)"), "$1") }
            .mapNotNull { it.toIntOrNull() }
            .maxOrNull() ?: 0
        return "テレポート #${(max + 1)}"
    }

    fun save() {
        val towns = mutableMapOf<String, MutableList<Map<String, *>>>()
        townTeleports.forEach { data ->
            towns.computeIfAbsent(data.townId.toString()) { mutableListOf() }.add(
                mapOf(
                    "town-id" to data.townId.toString(),
                    "name" to data.name,
                    "location" to data.location.run { "${(world ?: error("world is null")).name};$blockX;$blockY;$blockZ" },
                    "use-cost" to data.useCost,
                    "teleport-cost" to data.teleportCost,
                    "teleport-permission" to data.teleportPermission.map { it.lowercase },
                    "modify-permission" to data.modifyPermission.map { it.lowercase },
                )
            )
        }
        towns.forEach { (townId, list) ->
            config.set(townId, list)
        }
    }
}
