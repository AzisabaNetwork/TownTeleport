package net.azisaba.townteleport.data

import net.azisaba.townia.api.TowniaAPI
import net.azisaba.townia.data.Town
import net.azisaba.townia.data.TowniaPlayer
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

    fun hasPermissionToModify(resident: TowniaPlayer?, town: Town? = null): Boolean {
        if (resident == null) return false
        val mayorUuid = town?.mayorUuid
        if (mayorUuid != null && mayorUuid == resident.uuid) return true
        if (resident.townUuid == townId && resident.isMayor) return true
        if (modifyPermission.contains(ModifyPermissiveTarget.Assistant) && resident.isAssistant && resident.townUuid == townId) return true
        if (modifyPermission.contains(ModifyPermissiveTarget.Resident) && resident.townUuid == townId) return true
        return false
    }

    fun hasPermissionToTeleport(town: Town, player: Player): Boolean {
        if (player.hasPermission("townteleport.admin")) return true
        val api = TowniaAPI.get()
        val resident = api?.getResident(player.uniqueId)?.orElse(null)
        if (hasPermissionToModify(resident, town)) return true
        val isResident = resident != null && resident.townUuid == town.id
        if (teleportPermission.contains(TeleportPermissiveTarget.Resident) && isResident) return true
        if (teleportPermission.contains(TeleportPermissiveTarget.Nation) && town.nationUuid != null && resident != null && resident.townUuid != null) {
            val residentTown = api.getTown(resident.townUuid!!).orElse(null)
            if (residentTown != null && residentTown.nationUuid == town.nationUuid) return true
        }
        if (teleportPermission.contains(TeleportPermissiveTarget.Ally) && town.nationUuid != null && resident != null && resident.townUuid != null) {
            val nation = api.getNation(town.nationUuid!!).orElse(null)
            val residentTown = api.getTown(resident.townUuid!!).orElse(null)
            if (nation != null && residentTown != null && residentTown.nationUuid != null && nation.allies.contains(residentTown.nationUuid)) return true
        }
        if (teleportPermission.contains(TeleportPermissiveTarget.Outsider) && !isResident) return true
        return false
    }
}
