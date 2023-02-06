package xyz.bluspring.kilt.mixin;

import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockPathTypes.class)
public interface BlockPathTypesAccessor {
    @Invoker("<init>")
    static BlockPathTypes createBlockPathTypes(String name, int id, float f) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    static BlockPathTypes[] getValues() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    @Mutable
    static void setValues(BlockPathTypes[] values) {
        throw new IllegalStateException();
    }
}
