// TRACKED HASH: ec6c3c6702bb8f7588d211003582f0637ede7a14
package xyz.bluspring.kilt.forgeinjects.resources;

import io.github.fabricators_of_create.porting_lib.extensions.extensions.ResourceLocationExtensions;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.resources.ResourceLocationInjection;

@Mixin(ResourceLocation.class)
public abstract class ResourceLocationInject implements ResourceLocationInjection, ResourceLocationExtensions {
    @Override
    public int compareNamespaced(ResourceLocation o) {
        return ResourceLocationInjection.super.compareNamespaced(o);
    }
}