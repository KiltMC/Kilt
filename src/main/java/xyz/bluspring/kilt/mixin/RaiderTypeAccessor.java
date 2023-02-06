package xyz.bluspring.kilt.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Raid.RaiderType.class)
public interface RaiderTypeAccessor {
    @Invoker("<init>")
    static Raid.RaiderType createRaiderType(String name, int id, EntityType entityType, int[] is) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    static Raid.RaiderType[] getValues() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    @Mutable
    static void setValues(Raid.RaiderType[] values) {
        throw new IllegalStateException();
    }
}
