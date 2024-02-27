// TRACKED HASH: ec6c3c6702bb8f7588d211003582f0637ede7a14
package xyz.bluspring.kilt.forgeinjects.resources;

import io.github.fabricators_of_create.porting_lib.extensions.extensions.ResourceLocationExtensions;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.resources.ResourceLocationInjection;

@Mixin(ResourceLocation.class)
public class ResourceLocationInject implements ResourceLocationInjection, ResourceLocationExtensions {
    // i want a lawyer.
    @Shadow
    @Final
    protected String namespace;

    @Shadow
    @Final
    protected String path;

    @Override
    public int compareNamespaced(ResourceLocation o) {
        int ret = namespace.compareTo(o.getNamespace());
        return ret != 0 ? ret : this.path.compareTo(o.getPath());
    }
}