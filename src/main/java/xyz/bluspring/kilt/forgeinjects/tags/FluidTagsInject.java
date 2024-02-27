// TRACKED HASH: 820cf3db05b769c92b345f8be864731738c0d340
package xyz.bluspring.kilt.forgeinjects.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.tags.FluidTagsInjection;

@Mixin(FluidTags.class)
public class FluidTagsInject implements FluidTagsInjection {
    @CreateStatic
    private static TagKey<Fluid> create(ResourceLocation name) {
        return FluidTagsInjection.create(name);
    }
}