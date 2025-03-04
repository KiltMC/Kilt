package net.minecraftforge.fml

import java.util.function.BiPredicate
import java.util.function.Supplier

interface IExtensionPoint<T : Record> {
    @JvmRecord
    data class DisplayTest(val suppliedVersion: Supplier<String>, val remoteVersionTest: BiPredicate<String, Boolean>) : IExtensionPoint<DisplayTest> {
        constructor(version: String, remoteVersionTest: BiPredicate<String, Boolean>) : this(Supplier { version }, remoteVersionTest)

        companion object {
            // what the fuck is this
            @JvmField
            val IGNORESERVERONLY = "OHNOES\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31\\uD83D\\uDE31"

            @JvmField val IGNORE_SERVER_VERSION = Supplier { DisplayTest(IGNORESERVERONLY) { remoteVersion, isFromServer -> true } }
            @JvmField val IGNORE_ALL_VERSION = Supplier { DisplayTest("") { remoteVersion, isFromServer -> true } }
        }
    }
}