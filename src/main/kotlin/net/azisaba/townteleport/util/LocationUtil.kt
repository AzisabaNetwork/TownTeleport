package net.azisaba.townteleport.util

import org.bukkit.Location

object LocationUtil {
    fun Location.equalsBlockPos(another: Location) =
        blockX == another.blockX && blockY == another.blockY && blockZ == another.blockZ
}
