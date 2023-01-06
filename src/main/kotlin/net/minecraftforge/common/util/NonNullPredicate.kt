package net.minecraftforge.common.util

interface NonNullPredicate<T> {
    fun test(t: T): Boolean
}