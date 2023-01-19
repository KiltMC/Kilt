package net.minecraftforge.fml

import java.util.function.BiPredicate
import java.util.function.Supplier

interface IExtensionPoint<T : Record> {
    @JvmRecord
    data class DisplayTest(val suppliedVersion: Supplier<String>, val remoteVersionTest: BiPredicate<String, Boolean>) : IExtensionPoint<DisplayTest> {
        companion object {
            // what the fuck is this
            @JvmField
            val IGNORESERVERONLY = "OHNOES\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31"
        }
    }
}