package xyz.bluspring.kilt.injections.world.level.block.entity;

import java.util.Set;

import net.neoforged.neoforge.attachment.AttachmentType;
import xyz.bluspring.kilt.util.KiltHelper;
import xyz.bluspring.kilt.workarounds.AttachmentHolderWorkaround;

public interface BlockEntityInjection extends AttachmentHolderWorkaround {
    default Set<AttachmentType<?>> getAndClearAttachmentTypesToSync() {
        throw KiltHelper.createMixinException(BlockEntityInjection.class, "getAndClearAttachmentTypesToSync");
    }
}
