package xyz.bluspring.kilt.mixin.world.item;

import net.minecraft.world.item.FireworkRocketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireworkRocketItem.Shape.class)
public interface FireworkRocketItemShapeAccessor {
    @Invoker("<init>")
    static FireworkRocketItem.Shape createShape(String name, int ordinal, int id, String shapeName) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    static FireworkRocketItem.Shape[] getValues() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    @Mutable
    static void setValues(FireworkRocketItem.Shape[] values) {
        throw new IllegalStateException();
    }
}
