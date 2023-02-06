package xyz.bluspring.kilt.mixin;

import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MobCategory.class)
public interface MobCategoryAccessor {
    @Invoker("<init>")
    static MobCategory createMobCategory(String name, int id, String string2, int j, boolean bl, boolean bl2, int k) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({"MixinAnnotationTarget"})
    @Accessor("$VALUES")
    static MobCategory[] getValues() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    @Mutable
    static void setValues(MobCategory[] values) {
        throw new IllegalStateException();
    }
}
