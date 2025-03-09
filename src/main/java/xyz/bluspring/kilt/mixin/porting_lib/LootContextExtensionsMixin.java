package xyz.bluspring.kilt.mixin.porting_lib;

import io.github.fabricators_of_create.porting_lib.loot.extensions.LootContextExtensions;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LootContextExtensions.class)
public interface LootContextExtensionsMixin extends io.github.fabricators_of_create.porting_lib.extensions.LootContextExtensions {
}
