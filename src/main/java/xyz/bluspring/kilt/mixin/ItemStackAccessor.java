package xyz.bluspring.kilt.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public interface ItemStackAccessor {
    @Accessor("components")
    PatchedDataComponentMap kilt$getComponents();
}
