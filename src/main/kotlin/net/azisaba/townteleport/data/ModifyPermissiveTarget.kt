package net.azisaba.townteleport.data

enum class ModifyPermissiveTarget {
    Assistant,
    Resident,
    ;

    val shortName = name[0].lowercase()

    val lowercase = name.lowercase()
}