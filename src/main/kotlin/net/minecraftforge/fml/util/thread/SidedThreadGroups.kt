package net.minecraftforge.fml.util.thread

import net.minecraftforge.fml.LogicalSide

object SidedThreadGroups {
    @JvmField
    val CLIENT = SidedThreadGroup(LogicalSide.CLIENT)

    @JvmField
    val SERVER = SidedThreadGroup(LogicalSide.SERVER)
}