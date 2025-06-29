package xyz.bluspring.kilt.forgeinjects.core.component;

import net.minecraft.core.component.DataComponentPatch;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DataComponentPatch.class)
public abstract class DataComponentPatchInject {
    // Kilt: we don't have to implement component validation
}
