package net.minecraftforge.fml.util.thread

import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.fml.LogicalSide

object EffectiveSide {
    @JvmStatic
    fun get(): LogicalSide {
        return LogicalSide.valueOf(FabricLoader.getInstance().environmentType.name)
    }
}