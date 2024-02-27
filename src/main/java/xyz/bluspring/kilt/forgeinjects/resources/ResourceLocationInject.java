// TRACKED HASH: ec6c3c6702bb8f7588d211003582f0637ede7a14
package xyz.bluspring.kilt.forgeinjects.resources;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.resources.ResourceLocationInjection;

@Mixin(ResourceLocation.class)
public class ResourceLocationInject implements ResourceLocationInjection {
}