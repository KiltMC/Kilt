package net.neoforged.fml.util.thread

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