package net.minecraftforge.registries.holderset

import net.minecraft.core.HolderSet
import net.minecraftforge.common.extensions.IForgeHolderSet

interface ICustomHolderSet<T> : HolderSet<T>, IForgeHolderSet<T> {
    fun type(): HolderSetType<T>
}