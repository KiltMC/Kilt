package net.minecraftforge.registries.holdersets

import net.minecraft.core.HolderSet
import net.minecraftforge.common.extensions.IForgeHolderSet

interface ICustomHolderSet<T> : HolderSet<T>, IForgeHolderSet<T> {
    fun type(): HolderSetType<T>
}