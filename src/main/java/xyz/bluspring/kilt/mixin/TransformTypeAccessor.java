package xyz.bluspring.kilt.mixin;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemTransforms.TransformType.class)
public interface TransformTypeAccessor {
    @Invoker("<init>")
    static ItemTransforms.TransformType createTransformType(String name, int id) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    static ItemTransforms.TransformType[] getValues() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    @Mutable
    static void setValues(ItemTransforms.TransformType[] values) {
        throw new IllegalStateException();
    }
}
