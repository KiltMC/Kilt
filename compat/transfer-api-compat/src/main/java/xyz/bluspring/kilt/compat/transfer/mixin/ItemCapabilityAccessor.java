package xyz.bluspring.kilt.compat.transfer.mixin;

import java.util.List;
import java.util.Map;

import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemCapability.class)
public interface ItemCapabilityAccessor<T, C extends @Nullable Object> {
    @Accessor("providers")
    Map<Item, List<ICapabilityProvider<ItemStack, C, T>>> kilt$getProviders();

    @Mutable
    @Accessor("providers")
    void kilt$setProviders(Map<Item, List<ICapabilityProvider<ItemStack, C, T>>> providers);
}
