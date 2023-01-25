package xyz.bluspring.kilt.mixin;

import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeTab.class)
public interface CreativeModeTabAccessor {
    @Mutable
    @Accessor("TABS")
    static void setTabs(CreativeModeTab[] TABS) {
        throw new UnsupportedOperationException();
    }
}
