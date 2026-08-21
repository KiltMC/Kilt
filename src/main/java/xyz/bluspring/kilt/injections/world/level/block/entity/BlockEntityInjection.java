package xyz.bluspring.kilt.injections.world.level.block.entity;

import java.util.Set;

import net.neoforged.neoforge.attachment.AttachmentType;
import xyz.bluspring.kilt.util.KiltHelper;

public interface BlockEntityInjection {
    default Set<AttachmentType<?>> getAndClearAttachmentTypesToSync() {
        throw KiltHelper.createMixinException(BlockEntityInjection.class, "getAndClearAttachmentTypesToSync");
    }
}
