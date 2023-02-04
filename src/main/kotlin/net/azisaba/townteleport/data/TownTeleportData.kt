package net.azisaba.townteleport.data

import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

data class TownTeleportData(
    val townId: UUID,
    var name: String,
    val location: Location,
    var useCost: Double,
    var teleportCost: Double,
    val teleportPermission: MutableSet<TeleportPermissiveTarget>,
    val modifyPermission: MutableSet<ModifyPermissiveTarget>,
) {
    init {
        if (location.world == null) {
            throw RuntimeException("Location's world is null: $location")
        }
    }

    fun hasPermissionToModify(resident: Resident?): Boolean {
        if (resident == null) return false
        if (resident.isMayor) return true
        if (modifyPermission.contains(ModifyPermissiveTarget.Assistant) && resident.town.hasAssistant(resident)) return true
        if (modifyPermission.contains(ModifyPermissiveTarget.Resident)) return true
        return false
    }

    fun hasPermissionToTeleport(town: Town, player: Player): Boolean {
        if (player.hasPermission("townteleport.admin")) return true
        val resident = town.residents.find { it.name == player.name }
        if (hasPermissionToModify(resident)) return true
        if (teleportPermission.contains(TeleportPermissiveTarget.Resident) && resident != null) return true
        if (teleportPermission.contains(TeleportPermissiveTarget.Nation) && town.nation.residents.any { it.name == player.name }) return true
        if (teleportPermission.contains(TeleportPermissiveTarget.Ally) && town.nation.allies.any { it.residents.any { r -> r.name == player.name } }) return true
        if (teleportPermission.contains(TeleportPermissiveTarget.Outsider) && resident == null) return true
        return false
    }
}
