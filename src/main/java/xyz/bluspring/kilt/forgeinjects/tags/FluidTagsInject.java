package xyz.bluspring.kilt.forgeinjects.tags;

import net.minecraft.tags.FluidTags;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.tags.FluidTagsInjection;

@Mixin(FluidTags.class)
public class FluidTagsInject implements FluidTagsInjection {
}
