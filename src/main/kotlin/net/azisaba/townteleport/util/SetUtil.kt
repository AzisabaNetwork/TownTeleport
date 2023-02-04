package net.azisaba.townteleport.util

fun <T> MutableSet<T>.toggle(element: T) {
    if (contains(element)) {
        remove(element)
    } else {
        add(element)
    }
}
