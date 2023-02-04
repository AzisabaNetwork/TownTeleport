package net.azisaba.townteleport.data

enum class TeleportPermissiveTarget(val description: String) {
    Resident("Townに所属している市民。\nNationやAllyなどは含みません。"),
    Nation("国(Nation)に所属しているTownに所属している市民。\nAllyは含みません。"),
    Ally("同盟国(Ally)に所属しているTownに所属している市民。\nNationは含みません。"),
    Outsider("ポータルが設置されているTownに所属していないプレイヤー。\nNationやAllyはOutsiderに含まれます。"),
    ;

    val shortName = name[0].lowercase()

    val lowercase = name.lowercase()
}