package xyz.bluspring.kilt.mixin;

import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.IntFunction;

@Mixin(DyeColor.class)
public interface DyeColorAccessor {
    @Accessor("BY_ID")
    static IntFunction<DyeColor> getById() {
        throw new UnsupportedOperationException();
    }
}
