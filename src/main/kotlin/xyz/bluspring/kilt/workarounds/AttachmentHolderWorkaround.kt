package xyz.bluspring.kilt.workarounds

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag

interface AttachmentHolderWorkaround {
    fun deserializeAttachments(provider: HolderLookup.Provider, tag: CompoundTag)
    fun serializeAttachments(provider: HolderLookup.Provider): CompoundTag?
}