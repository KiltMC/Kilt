package xyz.bluspring.kilt.mixin;

import net.minecraft.world.entity.SpawnPlacements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpawnPlacements.Type.class)
public interface TypeAccessor {
    @Invoker("<init>")
    static SpawnPlacements.Type createType(String name, int id) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    static SpawnPlacements.Type[] getValues() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    static void setValues(SpawnPlacements.Type[] values) {
        throw new IllegalStateException();
    }
}
