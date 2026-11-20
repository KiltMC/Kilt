package xyz.bluspring.kilt.mixin.compat.fabric_api;

import net.neoforged.neoforge.common.extensions.ItemInstanceExtension;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.item.v1.FabricItemStack;

@Mixin(FabricItemStack.class)
public interface FabricItemStackMixin extends ItemInstanceExtension {
}
