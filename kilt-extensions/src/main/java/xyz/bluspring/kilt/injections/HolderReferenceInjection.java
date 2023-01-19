package xyz.bluspring.kilt.injections;

import net.minecraft.core.Holder;

public interface HolderReferenceInjection {
    default Holder.Reference.Type getType() {
        throw new RuntimeException("mixin, why didn't you add this");
    }
}
