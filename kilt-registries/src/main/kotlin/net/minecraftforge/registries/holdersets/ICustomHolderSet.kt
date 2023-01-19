package net.minecraftforge.registries.holdersets

import net.minecraft.core.HolderSet
import net.minecraftforge.common.extensions.IForgeHolderSet

interface ICustomHolderSet<T> : HolderSet<T>, IForgeHolderSet {
    fun type(): HolderSetType
}