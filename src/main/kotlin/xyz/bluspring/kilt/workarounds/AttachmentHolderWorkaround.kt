package xyz.bluspring.kilt.workarounds

import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

interface AttachmentHolderWorkaround {
    fun deserializeAttachments(input: ValueInput)
    fun serializeAttachments(tag: ValueOutput)
}
