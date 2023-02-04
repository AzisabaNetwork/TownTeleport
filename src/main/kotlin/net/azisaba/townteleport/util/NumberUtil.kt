package net.azisaba.townteleport.util

fun String.parseNumber(): Double? {
    if (this.isBlank()) return null
    if (!this.last().isDigit()) {
        val d = this.dropLast(1).toDoubleOrNull() ?: return null
        return when (this.last().lowercaseChar()) {
            'k' -> d * 1000
            'm' -> d * 1000000
            'b' -> d * 1000000000
            't' -> d * 1000000000000
            'q' -> d * 1000000000000000
            '千' -> d * 1000
            '万' -> d * 10000
            '億' -> d * 100000000
            '兆' -> d * 1000000000000
            '京' -> d * 10000000000000000
            else -> return null
        }
    }
    return toDoubleOrNull()
}

fun Double.toReadableString(): String =
    when {
        this >= 1000000000000000 -> "${(this / 1000000000000000).format(2)}Q"
        this >= 1000000000000 -> "${(this / 1000000000000).format(2)}T"
        this >= 1000000000 -> "${(this / 1000000000).format(2)}B"
        this >= 1000000 -> "${(this / 1000000).format(2)}M"
        this >= 1000 -> "${(this / 1000).format(2)}k"
        else -> this.format(2)
    }

fun Double.format(digits: Int) = "%.${digits}f".format(this)
