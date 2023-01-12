package net.minecraftforge.common.util

fun interface NonNullPredicate<T> {
    fun test(t: T): Boolean
}