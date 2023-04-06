package net.minecraftforge.fml.util.thread

import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.fml.LogicalSide

object EffectiveSide {
    @JvmStatic
    fun get(): LogicalSide {
        val group = Thread.currentThread().threadGroup
        return if (group is SidedThreadGroup)
            group.side
        else LogicalSide.CLIENT
    }
}