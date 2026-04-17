package xyz.bluspring.kilt.injects.data;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.data.DataProvider;

@Mixin(DataProvider.class)
public interface DataProviderInject {
    // Kilt: is this actually needed?
}
