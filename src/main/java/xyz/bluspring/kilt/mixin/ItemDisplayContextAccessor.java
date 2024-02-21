package xyz.bluspring.kilt.mixin;

import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemDisplayContext.class)
public interface ItemDisplayContextAccessor {
    @Invoker("<init>")
    static ItemDisplayContext createItemDisplayContext(String name, int id, int id2, String keyName) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({"MixinAnnotationTarget"})
    @Accessor("$VALUES")
    static ItemDisplayContext[] getValues() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    @Mutable
    static void setValues(ItemDisplayContext[] values) {
        throw new IllegalStateException();
    }
}
