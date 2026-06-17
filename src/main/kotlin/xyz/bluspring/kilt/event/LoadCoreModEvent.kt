package xyz.bluspring.kilt.event

import xyz.bluspring.kilt.loader.asm.coremod.CoreMod

fun interface LoadCoreModEvent {

    fun loadCoreMod(coremod: CoreMod): Boolean

}
