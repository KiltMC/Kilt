package xyz.bluspring.kilt.workarounds

import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.neoforge.attachment.IAttachmentHolder

interface AttachmentHolderWorkaround : IAttachmentHolder {
    fun deserializeAttachments(input: ValueInput)
    fun serializeAttachments(tag: ValueOutput)
}
