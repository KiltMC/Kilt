package xyz.bluspring.kilt.mixin;

import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DyeColor.class)
public interface DyeColorAccessor {
    @Accessor("BY_ID")
    static DyeColor[] getById() {
        throw new UnsupportedOperationException();
    }
}
